package ravensproject.transformations;

import java.util.*;

public final class CompositeTransformation implements Transformation {
    private final List<Transformation> transformations;

    public CompositeTransformation(final Collection<Transformation> transformations) {
        this.transformations = Collections.unmodifiableList(new ArrayList<>(transformations));
    }

    public CompositeTransformation(final Collection<Transformation> transformations, final Transformation last) {
        final List<Transformation> temp = new ArrayList<>(transformations);
        temp.add(last);
        this.transformations = Collections.unmodifiableList(temp);
    }

    public CompositeTransformation(final Transformation... transformations) {
        this.transformations = Collections.unmodifiableList(Arrays.asList(transformations));
    }

    @Override
    public int getCost() {
        int cost = 0;
        for(final Transformation t : transformations) {
            cost += t.getCost();
        }
        return cost;
    }

    @Override
    public Map<String, String> transform(final Map<String, String> toTransform) {
        Map<String, String> toRet = new HashMap<>(toTransform);
        for(final Transformation t : transformations) {
            toRet = t.transform(toRet);
            if(toRet.isEmpty()) {
                return toRet;
            }
        }
        return toRet;
    }
}
