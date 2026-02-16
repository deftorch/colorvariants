package com.colorvariants.core;

import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;

/**
 * Manages undo/redo functionality for color changes.
 */
public class UndoRedoManager {
    
    private static final Logger LOGGER = LoggerFactory.getLogger(UndoRedoManager.class);
    private static final int MAX_HISTORY_SIZE = 50;
    
    private final Deque<ColorAction> undoStack = new ArrayDeque<>();
    private final Deque<ColorAction> redoStack = new ArrayDeque<>();
    
    /**
     * Records a color change action.
     */
    public void recordAction(Level world, BlockPos pos, ColorTransform oldTransform, ColorTransform newTransform) {
        ColorAction action = new ColorAction(world, pos, oldTransform, newTransform);
        undoStack.push(action);
        redoStack.clear(); // Clear redo stack on new action
        
        // Limit history size
        while (undoStack.size() > MAX_HISTORY_SIZE) {
            undoStack.removeLast();
        }
        
        LOGGER.debug("Recorded action: {} -> {}", oldTransform, newTransform);
    }
    
    /**
     * Records a batch of color change actions.
     */
    public void recordBatchAction(Level world, List<BlockPos> positions, ColorTransform transform) {
        BatchColorAction action = new BatchColorAction(world, positions, transform);
        undoStack.push(action);
        redoStack.clear();
        
        while (undoStack.size() > MAX_HISTORY_SIZE) {
            undoStack.removeLast();
        }
        
        LOGGER.debug("Recorded batch action: {} blocks", positions.size());
    }
    
    /**
     * Undoes the last action.
     */
    public boolean undo() {
        if (undoStack.isEmpty()) {
            return false;
        }
        
        ColorAction action = undoStack.pop();
        action.undo();
        redoStack.push(action);
        
        LOGGER.debug("Undo performed");
        return true;
    }
    
    /**
     * Redoes the last undone action.
     */
    public boolean redo() {
        if (redoStack.isEmpty()) {
            return false;
        }
        
        ColorAction action = redoStack.pop();
        action.redo();
        undoStack.push(action);
        
        LOGGER.debug("Redo performed");
        return true;
    }
    
    /**
     * Checks if undo is available.
     */
    public boolean canUndo() {
        return !undoStack.isEmpty();
    }
    
    /**
     * Checks if redo is available.
     */
    public boolean canRedo() {
        return !redoStack.isEmpty();
    }
    
    /**
     * Clears all history.
     */
    public void clear() {
        undoStack.clear();
        redoStack.clear();
        LOGGER.debug("History cleared");
    }
    
    /**
     * Gets the number of undo actions available.
     */
    public int getUndoSize() {
        return undoStack.size();
    }
    
    /**
     * Gets the number of redo actions available.
     */
    public int getRedoSize() {
        return redoStack.size();
    }
    
    /**
     * Represents a single color change action.
     */
    private static class ColorAction {
        protected final Level world;
        protected final BlockPos pos;
        protected final ColorTransform oldTransform;
        protected final ColorTransform newTransform;
        
        public ColorAction(Level world, BlockPos pos, ColorTransform oldTransform, ColorTransform newTransform) {
            this.world = world;
            this.pos = pos;
            this.oldTransform = oldTransform;
            this.newTransform = newTransform;
        }
        
        public void undo() {
            ColorTransformManager manager = ColorTransformManager.get(world);
            if (oldTransform.isNone()) {
                manager.removeTransform(pos);
            } else {
                manager.setTransform(pos, oldTransform);
            }
        }
        
        public void redo() {
            ColorTransformManager manager = ColorTransformManager.get(world);
            if (newTransform.isNone()) {
                manager.removeTransform(pos);
            } else {
                manager.setTransform(pos, newTransform);
            }
        }
    }
    
    /**
     * Represents a batch color change action.
     */
    private static class BatchColorAction extends ColorAction {
        private final List<BlockPos> positions;
        private final Map<BlockPos, ColorTransform> oldTransforms;
        
        public BatchColorAction(Level world, List<BlockPos> positions, ColorTransform newTransform) {
            super(world, null, null, newTransform);
            this.positions = new ArrayList<>(positions);
            this.oldTransforms = new HashMap<>();
            
            // Store old transforms
            ColorTransformManager manager = ColorTransformManager.get(world);
            for (BlockPos pos : positions) {
                oldTransforms.put(pos, manager.getTransform(pos));
            }
        }
        
        @Override
        public void undo() {
            ColorTransformManager manager = ColorTransformManager.get(world);
            for (BlockPos pos : positions) {
                ColorTransform oldTransform = oldTransforms.get(pos);
                if (oldTransform != null && !oldTransform.isNone()) {
                    manager.setTransform(pos, oldTransform);
                } else {
                    manager.removeTransform(pos);
                }
            }
        }
        
        @Override
        public void redo() {
            ColorTransformManager manager = ColorTransformManager.get(world);
            for (BlockPos pos : positions) {
                if (newTransform.isNone()) {
                    manager.removeTransform(pos);
                } else {
                    manager.setTransform(pos, newTransform);
                }
            }
        }
    }
}
