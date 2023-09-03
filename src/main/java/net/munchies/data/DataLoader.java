package net.munchies.data;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import net.dehydration.DehydrationMain;
import net.dehydration.api.HydrationTemplate;
import net.fabricmc.fabric.api.resource.SimpleSynchronousResourceReloadListener;
import net.minecraft.item.Item;
import net.minecraft.registry.Registries;
import net.minecraft.resource.Resource;
import net.minecraft.resource.ResourceManager;
import net.minecraft.util.Identifier;
import net.minecraft.util.JsonHelper;
import net.minecraft.util.Pair;
import net.munchies.MunchiesMain;
import net.munchies.api.MunchiesTemplate;

import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.*;

public class DataLoader implements SimpleSynchronousResourceReloadListener {

    // Map to store replacing bools
    private HashMap<Integer, Boolean> healAmountReplaceList = new HashMap<Integer, Boolean>();
    private HashMap<Integer, Boolean> stackOverrideReplaceList = new HashMap<Integer, Boolean>();

    // Maps to store intermediate data.
    private List<Item> affectedItems = new ArrayList<>();
    private HashMap<Item, Integer> healAmountList = new HashMap<>();
    private HashMap<Item, Integer> stackOverrideList = new HashMap<>(); // todo if stack size is not divisible evenly by 4, judge user.


    @Override
    public Identifier getFabricId() {return new Identifier("munchies", "loader");}

    @Override
    public void reload(ResourceManager manager){
        MunchiesMain.LOGGER.debug("reload called");
        var healAmountResources = manager.findResources("food_healing_amounts", id -> id.getPath().endsWith(".json"));
        var stackSizeOverrideResources = manager.findResources("food_stack_size_overrides", id -> id.getPath().endsWith(".json"));

        affectedItems.clear();
        MunchiesMain.LOGGER.debug("processing heal amounts");
        processResources(healAmountResources, healAmountList, healAmountReplaceList);
        MunchiesMain.LOGGER.debug("processing stack override amounts");
        processResources(stackSizeOverrideResources, stackOverrideList, stackOverrideReplaceList);

        MunchiesMain.LOGGER.debug("generating pairs");
        HashMap<Pair<Integer, Integer>, ArrayList<Item>> templateGroups = new HashMap<>();
        affectedItems.forEach(item -> {
            Integer healAmount = healAmountList.getOrDefault(item, 0);
            Integer stackOverride = stackOverrideList.getOrDefault(item, -1);
            var pair = new Pair<Integer, Integer>(healAmount, stackOverride);
            if (templateGroups.containsKey(pair)){
                templateGroups.get(pair).add(item);
            } else {
                ArrayList<Item> list = new ArrayList<Item>();
                list.add(item);
                templateGroups.put(pair, list);
            }
        });

        templateGroups.forEach((pair, items) -> {
            MunchiesMain.LOGGER.debug("Adding template." + pair.toString() + items.toString());
            MunchiesMain.MUNCHIES_TEMPLATES.add(new MunchiesTemplate(pair.getLeft(), pair.getRight(), items));
        });
    }

    private void processResources(Map<Identifier, Resource> resources, HashMap<Item, Integer> valueList, HashMap<Integer, Boolean> replaceList){
        valueList.clear();
        replaceList.clear();
        resources.forEach((id, resourceRef) -> {
            try {
                InputStream stream = resourceRef.getInputStream();
                JsonObject data = JsonParser.parseReader(new InputStreamReader(stream)).getAsJsonObject();

                for (int i = 1; i <= 20; i++) {
                    replaceList.put(i, false); // replace is initially false in map.
                    JsonElement jsonElement = data.get(String.valueOf(i)); // get token of key "i"
                    if (jsonElement != null && jsonElement instanceof JsonObject) { // token must exist and be an object
                        JsonObject jsonObject = (JsonObject) jsonElement;

                        if (JsonHelper.getBoolean(jsonObject, "replace", false))
                            replaceList.replace(i, true); // update replace.

                        if (jsonObject.getAsJsonArray("items") != null) { // get items

                            if (JsonHelper.getBoolean(jsonObject, "replace", false)) {
                                // if this resource is 'replacing' then remove items from hydration template
                                Iterator<HydrationTemplate> iterator = DehydrationMain.HYDRATION_TEMPLATES.iterator();
                                while (iterator.hasNext())
                                    if (iterator.next().getHydration() == i)
                                        iterator.remove();
                            } else if (replaceList.get(i))
                                // if this object is not 'replacing' and another object is, then skip this.
                                continue;

                            for (int u = 0; u < jsonObject.getAsJsonArray("items").size(); u++) {
                                var itemIdentifier = new Identifier(jsonObject.getAsJsonArray("items").get(u).getAsString());
                                if (!Registries.ITEM.containsId(itemIdentifier)) {
                                    MunchiesMain.LOGGER.warn("{} is not a valid item identifier", itemIdentifier);
                                    continue;
                                }
                                var item = Registries.ITEM.get(itemIdentifier);
                                valueList.put(item, i);
                                affectedItems.add(item);
                            }
                        }

                    } else
                        continue;
                }

            } catch (Exception e) {
                DehydrationMain.LOGGER.error("Error occurred while loading resource {}. {}", id.toString(), e.toString());
            }
        });

    }
}
