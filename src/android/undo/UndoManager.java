package com.kcchen.nativecanvas.undo;

import android.text.TextUtils;
import android.util.Log;

import com.kcchen.nativecanvas.enums.UNDO_TYPE;
import com.kcchen.nativecanvas.sticker.Sticker;
import com.kcchen.nativecanvas.utils.Utility;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

/**
 * Manages the undo and redo stacks.
 * <p>
 * Used by first constructing an instance with a max undo stack size. When the user performs an
 * undoable operation, an instance of UndoItem must be created that contains both the type of
 * operation and the data needed to undo the operation. This is passed the function pushUndoItem().
 * When performing the undo, call the function popUndoItem() which will first push the UndoItem to
 * the redo stack and also return it to the caller. The details of unrolling an action is left up to
 * the receiver. To redo an action, call popRedoItem() to have it pushed to the UndoStack and have
 * it returned.
 */
public class UndoManager {
    private static final String TAG = UndoManager.class.getSimpleName();
    private static final String UM_DATA = "UmData";

    private LinkedList<UndoItem> undoItems = new LinkedList<UndoItem>();
    private LinkedList<UndoItem> redoItems = new LinkedList<UndoItem>();

    private int maxUndos;
    private List<OnUndoListener> listeners = new ArrayList<OnUndoListener>();
    private List<Integer> resets = new ArrayList<Integer>();
    private boolean isChanged = false;
    private boolean isMove = false;
    private int hash;

    public UndoManager(int maxUndos) {
        this.maxUndos = maxUndos;
        hash = this.toString().hashCode();
    }

    public UndoItem get(int index) {
        return undoItems.get(index);
    }

    public int getResetPoint() {
        int shortestDistance = 0;
        for (Integer reset : resets) {
            int distance = undoCount() - reset;
            if (shortestDistance == 0 && distance >= 0) shortestDistance = distance;
            if (distance > 0) shortestDistance = Math.min(shortestDistance, distance);
            Log.i(TAG, "> getResetPoint " + reset
                    + "\n undoCount:" + undoCount()
                    + "\n distance:" + distance
                    + "\n shortestDistance:" + shortestDistance
                    + "\n result:" + (undoCount() - shortestDistance)
            );
        }
        if (shortestDistance != 0) {
            return undoCount() - shortestDistance;
        } else {
            return 0;
        }
    }

    /**
     * Pushes an item onto the undo stack. If there isn't enough space, removes the first/oldest item
     * on the stack
     *
     * @param undoItem The item to be stacked
     */
    public void add(UndoItem undoItem) {
        // Loses any future commands that were in the redo stack
        if (redoItems.size() > 0) {
            redoItems.clear();
            ArrayList<Integer> removes = new ArrayList<Integer>();
            for (Integer reset : resets) {
                if (reset > undoCount()) {
                    removes.add(reset);
                }
            }
            for (Integer remove : removes) {
                resets.remove(remove);
            }
        }

        if (undoItem.getType() == UNDO_TYPE.CLEAR) {
            if (!resets.contains(undoCount())) resets.add(undoCount());
        }

        if (undoItem.getType() == UNDO_TYPE.STICKER_IMAGE || undoItem.getType() == UNDO_TYPE.STICKER_TEXT) {
            setTransform(undoItem.getData().optString(Sticker.STICKER_UUID), false);
        }
        // Maintains the maximum number of undos (and hence the maximum number of redos)
        undoItem.setIndex(undoItems.size());
        undoItems.push(undoItem);
        isChanged = true;
        if (maxUndos > 0 && undoItems.size() > maxUndos) {
            undoItems.pollLast();
            isChanged = false;
        }
        sendChanged();
        Log.i(TAG, "> NCN undo " + undoCount());
    }

    private void setTransform(String uuid, boolean isKeepLast) {
        boolean isUpdateLast = false;
        for (int i = 0; i < undoItems.size(); i++) {
            UndoItem item = get(i);
            if ((item.getType() == UNDO_TYPE.STICKER_IMAGE || item.getType() == UNDO_TYPE.STICKER_TEXT) && item.getData().optString(Sticker.STICKER_UUID).equals(uuid)) {
                //Log.d(TAG,"> setTransform " + uuid + " - " + item.getData().optString(Sticker.STICKER_UUID));
                if (isKeepLast && !isUpdateLast) {
                    item.setTransform(false);
                    isUpdateLast = true;
                } else {
                    item.setTransform(true);
                }
            }
        }
    }

    private void sendChanged() {
        if (isChanged && !isMove) {
            for (OnUndoListener listener : listeners) {
                //Log.d(TAG, "> NCN sendChanged(" + (listeners.size()) + ") " + listener);
                listener.onChanged();
            }
        }
    }

    /**
     * Pops the top of the undo stack and pushes it to the redo stack
     *
     * @return The UndoItem on the top of the stack or null if there was no such element
     */
    public UndoItem undo() {
        UndoItem undoItem = undoItems.pollFirst();
        isChanged = true;
        // Pushes item onto redo stack
        if (undoItem != null) {
            redoItems.push(undoItem);
        } else {
            isChanged = false;
        }
        if (isChanged) {
            if (undoItem.getType() == UNDO_TYPE.STICKER_IMAGE || undoItem.getType() == UNDO_TYPE.STICKER_TEXT) {
                setTransform(undoItem.getData().optString(Sticker.STICKER_UUID), true);
            }
        }
        sendChanged();
        return undoItem;
    }

    public boolean move(int step) {
        if (step < 0) {
            isMove = true;
            for (int i = 1; i <= -step; i++) {
                undo();
            }
            isMove = false;
            sendChanged();
            return true;
        } else if (step > 0) {
            isMove = true;
            for (int i = 1; i <= step; i++) {
                redo();
            }
            isMove = false;
            sendChanged();
            return true;
        }
        return false;
    }

    /**
     * Pops the top of the redo stack and pushes it to the undo stack
     *
     * @return The UndoItem on the top of the stack or null if there was no such element
     */
    public UndoItem redo() {
        UndoItem redoItem = redoItems.pollFirst();
        isChanged = true;

        // Pushes item onto undo stack
        if (redoItem != null) {
            undoItems.push(redoItem);
        } else {
            isChanged = false;
        }
        if (isChanged) {
            if (redoItem.getType() == UNDO_TYPE.STICKER_IMAGE || redoItem.getType() == UNDO_TYPE.STICKER_TEXT) {
                setTransform(redoItem.getData().optString(Sticker.STICKER_UUID), true);
            }
        }
        sendChanged();
        return redoItem;
    }

    public boolean canUndo() {
        return undoItems.size() > 0;
    }

    public boolean canRedo() {
        return redoItems.size() > 0;
    }

    public int undoCount() {
        return undoItems.size();
    }

    public int undoCountFromReset() {
        return undoItems.size() - getResetPoint();
    }

    public int redoCount() {
        return redoItems.size();
    }

    public int getCurrent() {
        return (undoCount() - 1 >= 0) ? undoCount() - 1 : 0;
    }

    /**
     * Clears the both the undo and redo stacks, a fresh start.
     */
    public void clear() {
        if (undoItems != null) {
            undoItems.clear();
        }
        if (redoItems != null) {
            redoItems.clear();
        }
        isChanged = true;
        sendChanged();
    }

    public void release() {
        if (resets != null) {
            resets.clear();
            resets = null;
        }
        if (listeners != null) {
            listeners.clear();
            listeners = null;
        }
        if (undoItems != null) {
            undoItems.clear();
            undoItems = null;
        }
        if (redoItems != null) {
            redoItems.clear();
            redoItems = null;
        }
    }

    public void setOnUndoListener(OnUndoListener listener) {
        if (listener != null && !listeners.contains(listener)) {
            //Log.d(TAG, "> NCN setOnUndoListener(" + (listeners.size() + 1) + ") " + listener);
            listeners.add(listener);
        }
    }

    public void removeOnUndoListener(OnUndoListener listener) {
        if (listener != null && listeners.contains(listener)) {
            //Log.d(TAG, "> NCN removeOnUndoListener(" + (listeners.size() - 1) + ") " + listener);
            listeners.remove(listener);
        }
    }

    public List<Integer> getResets() {
        return resets;
    }

    public void setData(JSONArray data) {
        for (int i = 0; i < data.length(); i++) {
            try {
                JSONObject item = data.optJSONObject(i);
                Log.w(TAG, "> setData " + item.optString(UndoItem.UNDO_ITEM_TYPE));
                //Log.i(TAG,"> setData " + JsonFormatter.format(item));
                if (item != null) {
                    UNDO_TYPE type = UNDO_TYPE.get(item.optString(UndoItem.UNDO_ITEM_TYPE));
                    adjustUndoItems(i);
                    switch (type) {
                        case DRAWING:
                        case ERASER:
                            undoItems.add(i, new UndoItemDrawing(item));
                            break;
                        case STICKER_IMAGE:
                            undoItems.add(i, new UndoItem(item));
                            break;
                        case STICKER_TEXT:
                            undoItems.add(i, new UndoItem(item));
                            break;
                        case TRANSFORM:
                            break;
                        case TEMPLATE:
                            break;
                        case CLEAR:
                            undoItems.add(i, new UndoItem(item));
                            if (!resets.contains(undoCount())) resets.add(i);
                            break;
                        default:
                            break;
                    }
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        isChanged = true;
        sendChanged();
        //Log.i(TAG, "> NCN setData " + undoCount());
    }

    private void adjustUndoItems(int index) {
        if (this.undoItems.size() <= index) {
            for (int i = this.undoItems.size(); i < index; i++) {
                this.undoItems.add(i, null);
            }
        }
    }

    public boolean isEmpty() {
        return undoItems.size() == 0 && redoItems.size() == 0;
    }

    public boolean isUpdate() {
        Log.e(TAG, "> NCN isUpdate " + this.toString().hashCode() + " - " + hash + "   json:" + Utility.getJsonHash(this));
        return hash != this.toString().hashCode();
    }

    public LinkedList<UndoItem> getUndoItems() {
        return undoItems;
    }

    public interface OnUndoListener {
        void onChanged();

        void onImport(UndoManager undoManager);
    }

    public JSONObject exportData() {
        JSONObject data = new JSONObject();
        try {
            removeNull();
            data.put(UM_DATA, new JSONArray(this.toString()));
        } catch (Exception e) {
            e.printStackTrace();
        }
        return data;
    }

    private void removeNull() {
        List<Integer> remove = new ArrayList<Integer>();
        for (int i = 0; i < undoItems.size(); i++) {
            if (get(i) == null) {
                remove.add(i);
            }
        }
        if (remove.size() > 0) {
            //Log.e(TAG,"> NCN removeNull " + remove);
            for (int i = remove.size() - 1; i >= 0; i--) {
                undoItems.remove(remove.get(i));
            }
        }
    }

    private JSONArray removeNull(JSONArray jsonArray) {
        JSONArray cleanJsonArray = new JSONArray();
        for (int i = 0; i < jsonArray.length(); i++) {
            try {
                if (jsonArray.get(i) != null) {
                    cleanJsonArray.put(jsonArray.optJSONObject(i));
                }
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
        return cleanJsonArray;
    }

    public boolean importData(JSONObject data) {
        boolean isImported = false;
        try {
            clear();
            JSONArray jsonArray = data.optJSONArray(UM_DATA);
            setData(removeNull(jsonArray));
            removeNull();
            hash = this.toString().hashCode();
            //Log.i(TAG, "> importData " + listeners + " " + (listeners != null ? listeners.size() : 0));
            if (listeners != null) {
                for (OnUndoListener listener : listeners) {
                    listener.onImport(this);
                }
            }
            isImported = true;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return isImported;
    }

    @Override
    public String toString() {
        return "["
                + "\n" + TextUtils.join(",\n", undoItems)
                + "\n]"
                ;
    }
}
