package com.zeromods.core.forge;
import com.zeromods.core.network.*;
import net.minecraft.core.*;
import net.minecraft.nbt.*;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.saveddata.SavedData;
import java.util.*;
/** One store per dimension and host-chosen data name. Schema 1 preserves common network state. */
public final class NetworkSavedData extends SavedData {
    private final NetworkDirectory<BlockPos> directory = new NetworkDirectory<>();
    public NetworkDirectory<BlockPos> directory() { return directory; }
    public static NetworkSavedData get(ServerLevel level, String name) {
        if (!name.matches("[a-z0-9_]+")) throw new IllegalArgumentException("Invalid save name");
        return level.getDataStorage().computeIfAbsent(NetworkSavedData::load, NetworkSavedData::new,name);
    }
    public static NetworkSavedData load(CompoundTag data) {
        if(data.getInt("Schema") != 1) throw new IllegalArgumentException("Unsupported ZeroMods Core network schema");
        var store = new NetworkSavedData();
        var list=data.getList("Networks",Tag.TAG_COMPOUND);
        for(int i=0;i<list.size();i++) {
            var tag=list.getCompound(i);
            if(!tag.hasUUID("Id")) throw new IllegalArgumentException("Missing network identity");
            var network = new ManagedNetwork<BlockPos>(tag.getUUID("Id"),tag.getString("Kind"),tag.getString("Name"),tag.hasUUID("Owner")?tag.getUUID("Owner"):null);
            for(long pos:tag.getLongArray("Nodes")) network.addNode(BlockPos.of(pos));
            if(tag.contains("Anchor")) network.anchor(BlockPos.of(tag.getLong("Anchor")));
            var members=tag.getList("Members",Tag.TAG_COMPOUND);
            for(int m=0;m<members.size();m++) network.addMember(members.getCompound(m).getUUID("Id"));
            network.access(tag.getBoolean("PublicUse"),tag.getBoolean("Discoverable"));
            var properties=tag.getCompound("Properties");
            for(String key:properties.getAllKeys()) network.property(key,properties.getString(key));
            store.directory.put(network);
        }
        return store;
    }
    @Override public CompoundTag save(CompoundTag data) {
        data.putInt("Schema",1);var list=new ListTag();
        for(var network:directory.snapshot()) {
            var tag=new CompoundTag();tag.putUUID("Id",network.id());tag.putString("Kind",network.kind());tag.putString("Name",network.name());
            if(network.owner()!=null)tag.putUUID("Owner",network.owner());
            if(network.anchor()!=null)tag.putLong("Anchor",network.anchor().asLong());
            tag.putLongArray("Nodes",network.nodes().stream().mapToLong(BlockPos::asLong).sorted().toArray());
            var members=new ListTag();
            network.members().stream().sorted().forEach(id->{var member=new CompoundTag();member.putUUID("Id",id);members.add(member);});
            tag.put("Members",members);tag.putBoolean("PublicUse",network.publicUse());tag.putBoolean("Discoverable",network.discoverable());
            var properties=new CompoundTag();network.properties().forEach(properties::putString);tag.put("Properties",properties);list.add(tag);
        }
        data.put("Networks",list);return data;
    }
}
