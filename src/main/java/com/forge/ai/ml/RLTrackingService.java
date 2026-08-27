package com.forge.ai.ml;

import jakarta.annotation.PostConstruct;
import lombok.Getter;
import org.springframework.stereotype.Service;

import java.util.Arrays;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import static jnumpy.core.Arrays.*;

@Service
public class RLTrackingService {

    private static final String[] ACTIONS = {
        "increase_protein",
        "deload_week",
        "add_cardio",
        "increase_calories",
        "improve_sleep",
        "simplify_routine"
    };
    private static final double EPSILON = 0.15;
    private static final double LEARNING_RATE = 0.1;
    private static final double GAMMA = 0.9;

    private final Map<String, double[]> qTable = new ConcurrentHashMap<>();
    @Getter
    private final Map<String, Integer> actionCounts = new ConcurrentHashMap<>();
    @Getter
    private final Map<String, Double> totalRewards = new ConcurrentHashMap<>();

    @PostConstruct
    void init() {
        for (String action : ACTIONS) {
            actionCounts.put(action, 0);
            totalRewards.put(action, 0.0);
        }
    }

    public String recommend(String stateKey) {
        double[] qValues = qTable.computeIfAbsent(stateKey, k -> {
            double[] vals = new double[ACTIONS.length];
            Arrays.fill(vals, 0.0);
            return vals;
        });

        int actionIdx;
        if (Math.random() < EPSILON) {
            actionIdx = (int) (Math.random() * ACTIONS.length);
        } else {
            actionIdx = argmax(qValues);
        }

        actionCounts.merge(ACTIONS[actionIdx], 1, Integer::sum);
        return ACTIONS[actionIdx];
    }

    public void update(String stateKey, String action, double reward, String nextStateKey) {
        int actionIdx = indexOf(action);
        if (actionIdx < 0) return;

        double[] qValues = qTable.computeIfAbsent(stateKey, k -> new double[ACTIONS.length]);
        double[] nextQ = qTable.computeIfAbsent(nextStateKey, k -> new double[ACTIONS.length]);

        double maxNext = Double.NEGATIVE_INFINITY;
        for (double v : nextQ) {
            if (v > maxNext) maxNext = v;
        }

        double tdError = reward + GAMMA * (maxNext == Double.NEGATIVE_INFINITY ? 0 : maxNext) - qValues[actionIdx];
        qValues[actionIdx] += LEARNING_RATE * tdError;

        totalRewards.merge(action, reward, Double::sum);
    }

    public static String buildStateKey(String adherenceTier, String trend, boolean hasPlateau) {
        return adherenceTier + "|" + trend + "|" + hasPlateau;
    }

    public double[] getQValues(String stateKey) {
        return qTable.getOrDefault(stateKey, new double[ACTIONS.length]);
    }

    public String[] getActions() {
        return ACTIONS.clone();
    }

    private int argmax(double[] values) {
        int best = 0;
        for (int i = 1; i < values.length; i++) {
            if (values[i] > values[best]) best = i;
        }
        return best;
    }

    private int indexOf(String action) {
        for (int i = 0; i < ACTIONS.length; i++) {
            if (ACTIONS[i].equals(action)) return i;
        }
        return -1;
    }
}
