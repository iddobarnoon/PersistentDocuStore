package project.undo;

public interface Undoable {
    /**
     * @return true if the undo succeeds
     */
    boolean undo();
}
