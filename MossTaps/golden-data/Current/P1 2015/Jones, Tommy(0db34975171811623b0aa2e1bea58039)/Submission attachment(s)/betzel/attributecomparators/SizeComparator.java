package ravensproject.betzel.attributecomparators;

import ravensproject.betzel.helpers.DiffHelper;
import ravensproject.betzel.interfaces.IAttributeComparator;

import java.util.HashMap;

/**
 * Created by scott betzel on 6/1/15.
 */
public class SizeComparator
        implements IAttributeComparator {

    /**
     * These are the valid values for a Size Attribute (slot) - Note, if you edit these
     * that the order matters
     */
    private static String[] possibleValues = {"very small", "small", "medium", "large", "very large", "huge"};

    /**
     * This will contain the diff map created from Values
     */
    private static HashMap<String, Double> _valueMap;

    /**
     * This will populate the _valueMap
     */
    static {
        _valueMap = DiffHelper.createDiffMap(possibleValues);
    }

    /**
     * This will return the name of the attribute
     *
     * @return This will return the name of the attribute
     */
    @Override
    public String get_name() {
        return "size";
    }

    /**
     * This will return a number from 0 to 1 which represents how different
     * each attribute is.  0 means no difference, 1 means completely opposite
     *
     * @param fromAttributeValue the value you are converting from
     * @param toAttributeValue   the value you are converting to
     * @return Returns a double indicating the amount of differences.
     * 0 means no difference, 1 means completely opposite
     */
    @Override
    public double getDiff(String fromAttributeValue, String toAttributeValue) {
        if (fromAttributeValue.toLowerCase().equals(toAttributeValue.toLowerCase())) {
            return 0.0;
        }

        String key = DiffHelper.getKey(fromAttributeValue, toAttributeValue);
        if (!_valueMap.containsKey(key)) {
            return 1.0;
        }

        return _valueMap.get(key);
    }
}