package ravensproject;

import java.util.*;
import java.util.stream.Collectors;

/**
 * Created by jblac_000 on 6/2/2015.
 */
public class FigureNetwork {
    private RavensFigure figure;
    private List<RavensObject> objects;
    private List<TripleStore> relationships;
    private int objectCount;

    public FigureNetwork(RavensFigure figure) {
        this.figure = figure;
        this.objects = new ArrayList<>();
        this.relationships = new ArrayList<>();
        this.objectCount = figure.getObjects().size();
    }

    public int getObjectCount() {
        return objectCount;
    }

    public RavensFigure getFigure() {
        return figure;
    }

    public List<RavensObject> getObjects() {
        return objects;
    }

    public List<TripleStore> getRelationships() {
        return relationships;
    }

    public void buildNetwork() {
        figure.getObjects().forEach((k,v) -> objects.add(v));

        if(objects.size() > 1) {
            for(RavensObject o : objects) {
                o.getAttributes().forEach((k,v) -> {

                    switch(k){
                        case "above":
                        case "overlaps":
                            relationships.add(new TripleStore(o, k, figure.getObjects().get(v)));
                            break;
                        case "inside":
                            if(v.length() == 1)
                                relationships.add(new TripleStore(o, "inside", figure.getObjects().get(v)));
                            else {
                                String[] relatedObjs = v.split(",");

                                for(String s : relatedObjs)
                                    relationships.add(new TripleStore(o, "inside", figure.getObjects().get(s)));
                            }
                            break;
                    }
                });
            }
        }
    }

    public RavensObject getPrimaryObject(){
        if(this.objectCount == 1){
            return this.objects.get(0);
        }
        else {
            List<RavensObject> objList = new ArrayList<>(this.objects);

            for(TripleStore t : this.relationships){
                objList.remove(t.getPrimary());
            }

            return objList.get(0);
        }
    }

    public List<RavensObject> getObjectHierarchy() {
        List<RavensObject> hierarchy = new ArrayList<>();
        if(this.objectCount == 1){
            hierarchy.add(this.objects.get(0));
            return hierarchy;
        }

        Map<String, List<TripleStore>> groupedRelationships = relationships
                .stream()
                .collect(Collectors.groupingBy(TripleStore::getRelationship));

        if(groupedRelationships.keySet().size() == 1){


            Map<RavensObject, Long> relationCount = objects
                    .stream()
                    .collect(Collectors.toMap(o -> o, o -> relationships
                            .stream()
                            .filter(r -> r.getPrimary().equals(o))
                            .count()));

            while(relationCount.size() > 0){

                Map.Entry<RavensObject, Long> smallest = null;
                for(Map.Entry<RavensObject, Long> entry : relationCount.entrySet()){

                    if(smallest == null || entry.getValue().intValue() < smallest.getValue().intValue())
                        smallest = entry;
                }

                hierarchy.add(smallest.getKey());
                relationCount.remove(smallest.getKey());
            }
        }
        else {
            String[] relationshipList = { "overlaps", "above", "inside" };

            for(String s : relationshipList){

                if(groupedRelationships.containsKey(s)) {
                    Map<RavensObject, Long> relationCount = objects
                            .stream()
                            .collect(Collectors.toMap(o -> o, o -> groupedRelationships.get(s)
                                    .stream()
                                    .filter(r -> r.getPrimary().equals(o))
                                    .count()));

                    while(relationCount.size() > 0){

                        Map.Entry<RavensObject, Long> smallest = null;
                        for(Map.Entry<RavensObject, Long> entry : relationCount.entrySet()){

                            if(smallest == null || entry.getValue().intValue() < smallest.getValue().intValue())
                                smallest = entry;
                        }

                        hierarchy.add(smallest.getKey());
                        relationCount.remove(smallest.getKey());
                    }
                }
            }
        }

        return hierarchy;
    }
}
