package fr.nocsy.mcpets.data;

import lombok.Getter;
import lombok.Setter;
import org.jetbrains.annotations.NotNull;

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

public class PlayerSignal {

    private static final Map<UUID, SignalWrapper> orderMap = new ConcurrentHashMap<>();

    /**
     * Set the active signal for the player
     */
    public static void setSignal(UUID owner, String signal) {
        Pet pet = Pet.fromOwner(owner);

        if (pet == null)
            return;

        int indexSignal = getSignalIndex(pet, signal);
        if (indexSignal == -1)
            return;

        SignalWrapper wrapper = new SignalWrapper();
        wrapper.setActiveSignal(signal);
        wrapper.setIndexSignal(indexSignal);
        wrapper.setPet(pet);

        orderMap.put(owner, wrapper);
    }

    /**
     * Get the active signal of the player
     */
    public static String getSignalTag(UUID owner) {
        if (owner == null) return null;
        final SignalWrapper wrapper = orderMap.get(owner);
        if (wrapper != null) {
            return wrapper.getActiveSignal();
        }
        return null;
    }

    /**
     * Says whether or not the signal is compatible with the pet of the player
     */
    public static boolean isCompatible(UUID owner, String signal) {
        Pet pet = Pet.fromOwner(owner);

        if (!pet.isStillHere())
            return false;

        return pet.getSignals().contains(signal);
    }

    /**
     * Get the index of the signal of the pet (-1 if doesn't exists)
     */
    public static int getSignalIndex(@NotNull Pet pet, String signal) {
        if (pet.getSignals().isEmpty())
            return -1;

        int i = 0;
        for (String s : pet.getSignals()) {
            if (s.equalsIgnoreCase(signal))
                return i;
            i++;
        }
        return -1;
    }

    /**
     * Set the default signal to cast for the pet summoned
     */
    public static void setDefaultSignal(UUID owner, @NotNull Pet pet) {
        if (pet.getSignals().isEmpty())
            return;

        String signal = pet.getSignals().getFirst();
        setSignal(owner, signal);
    }

    public static String getNextSignal(UUID owner) {
        if (owner == null) return null;
        final SignalWrapper wrapper = orderMap.get(owner);
        if (wrapper != null) {
            Pet pet = Pet.fromOwner(owner);
            if (pet == null)
                return null;

            if (pet.getSignals().isEmpty())
                return null;

            int newIndex = wrapper.getIndexSignal() + 1;
            if (pet.getSignals().size() <= newIndex)
                newIndex = 0;

            return pet.getSignals().get(newIndex);
        }

        return null;
    }
}

class SignalWrapper {
    @Getter
    @Setter
    private String activeSignal;

    @Getter
    @Setter
    private int indexSignal;

    @Getter
    @Setter
    private Pet pet;
}
