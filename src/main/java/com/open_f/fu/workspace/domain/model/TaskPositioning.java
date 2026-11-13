package com.open_f.fu.workspace.domain.model;

import java.util.List;

/**
 * Regla de orden: calcula la positionY para dejar una tarjeta en el lugar "target"
 * de una columna. Recibe las posiciones de la columna ordenadas de menor a mayor.
 */
public final class TaskPositioning {

    private static final double GAP = 100.0;

    private TaskPositioning() {
    }

    public static double positionAt(List<Double> sortedPositions, int target) {
        if (sortedPositions.isEmpty()) {
            return Task.DEFAULT_POSITION;
        }
        if (target <= 0) {
            return sortedPositions.getFirst() / 2;
        }
        if (target >= sortedPositions.size()) {
            return sortedPositions.getLast() + GAP;
        }
        double prev = sortedPositions.get(target - 1);
        double next = sortedPositions.get(target);
        return (prev + next) / 2;
    }
}
