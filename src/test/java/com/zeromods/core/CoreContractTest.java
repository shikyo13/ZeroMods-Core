package com.zeromods.core;
import com.zeromods.core.network.*;
import com.zeromods.core.filter.*;
import com.zeromods.core.ui.*;
import com.zeromods.core.settings.*;
import com.zeromods.core.sync.*;
import com.zeromods.core.energy.*;
import com.zeromods.core.animation.*;
import com.zeromods.core.tutorial.*;
import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;
public final class CoreContractTest {
    static void check(boolean condition, String message) { if (!condition) throw new AssertionError(message); }
    static void rejects(Runnable action) { try { action.run(); } catch (IllegalArgumentException | UnsupportedOperationException e) { return; } throw new AssertionError("Expected rejection"); }
    public static void main(String[] args) {
        var graph = Map.of("a", List.of("b", "c"), "b", List.of("a", "c"), "c", List.of("a"));
        check(NetworkTraversal.connected("a", x -> x, graph::get).equals(List.of("a","b","c")), "cyclic traversal visits once in order");
        UUID owner=UUID.randomUUID(), member=UUID.randomUUID(), outsider=UUID.randomUUID();
        var network = new ManagedNetwork<String>(UUID.randomUUID(), "test:energy", "Home", owner);
        network.addNode("a"); network.addNode("b"); network.addMember(member); network.access(true, true);
        check(network.access().canUse(outsider) && !network.access().canConfigure(outsider), "public use must not permit mutations");
        check(network.access().canConfigure(member) && !network.access().canUse(null), "membership and anonymous access");
        var directory = new NetworkDirectory<String>(); directory.put(network); directory.setLoaded("a", true);
        var before = directory.snapshot(); directory.setLoaded("a", false);
        check(directory.snapshot().equals(before), "unload must not erase identity or nodes");
        directory.restore(before); check(directory.loadedNodes(network.id()).isEmpty(), "restore starts unloaded");
        var restored=directory.get(network.id()).orElseThrow();
        check(restored.name().equals("Home") && restored.memberIds().contains(member), "save round trip");
        rejects(() -> restored.nodes().clear());
        UUID childId=UUID.randomUUID(); directory.split(network.id(),childId,"Workshop",Set.of("b"));
        check(directory.get(childId).orElseThrow().nodes().equals(Set.of("b")), "split nodes");
        check(restored.id().equals(network.id()) && restored.anchor().orElseThrow().equals("a"), "split retains original identity");
        directory.merge(network.id(),childId); check(restored.nodes().equals(Set.of("a","b")), "merge preserves nodes");
        var foreign=new ManagedNetwork<String>(UUID.randomUUID(),"test:energy","Other",outsider); directory.put(foreign);
        rejects(() -> directory.merge(network.id(),foreign.id()));
        for (boolean eligible : List.of(false,true)) for (boolean category : List.of(false,true))
            for(boolean constraint : List.of(false,true)) for(boolean invert : List.of(false,true))
                check(FilterRule.result(eligible,category,constraint,invert)==(eligible && ((category && constraint)^invert)), "filter truth table");
        var rule=new FilterRule<Integer>(x -> x >= 0,List.of(x -> x % 2 == 0),List.of(x -> x < 10),true);
        check(!rule.test(-1) && rule.test(3) && !rule.test(2), "exemptions outside inversion");
        var fit=CanvasFit.fit(320,180,540,344,4);
        check(540*fit.scale()<=312.00001 && 344*fit.scale()<=172.00001, "small GUI fits");
        check(Math.abs(fit.pointer(150*fit.scale())-150)<1e-8,"pointer/render inverse");
        var schema=new SettingsSchema();schema.register(new Setting<>("test:color",Integer.class,0,"label","help",x -> x>=0 && x<=0xffffff));
        var session=new SettingsSession<UUID>(schema,owner::equals);
        check(session.apply(outsider,0,"test:color",1)==SettingsSession.Result.DENIED,"server checks permissions");
        check(session.apply(owner,0,"test:color",1)==SettingsSession.Result.APPLIED,"instant apply");
        check(session.apply(owner,0,"test:color",2)==SettingsSession.Result.STALE,"stale client rejected");
        check(session.apply(owner,1,"test:color",-1)==SettingsSession.Result.INVALID,"range rejected");
        check(session.apply(owner,1,"test:unknown",1)==SettingsSession.Result.INVALID,"unknown setting rejected");
        AtomicInteger dirty=new AtomicInteger();var energy=new EnergyBuffer(Long.MAX_VALUE,Long.MAX_VALUE,Long.MAX_VALUE,dirty::incrementAndGet);
        energy.receive(Long.MAX_VALUE,true);check(energy.stored()==0 && dirty.get()==0,"energy simulation inert");
        energy.receive(Long.MAX_VALUE,false);check(energy.receive(1,false)==0,"capacity never overflows");
        check(energy.extract(5,false)==5 && energy.stored()==Long.MAX_VALUE-5,"energy conservation");
        var physicalDirectory=new NetworkDirectory<String>();
        var reconciler=new PhysicalNetworkReconciler<>(physicalDirectory,"test:field");
        var initial=reconciler.reconcile(List.of(new PhysicalNetworkReconciler.Component<>(owner,"Fence",List.of("a","b","c"))),Set.of("a","b","c"));
        UUID fieldId=initial.get("a");
        var partial=reconciler.reconcile(List.of(new PhysicalNetworkReconciler.Component<>(owner,"Unused",List.of("c"))),Set.of("c"));
        check(partial.get("c").equals(fieldId) && physicalDirectory.get(fieldId).orElseThrow().nodes().size()==3,"unload retains physical network identity");
        var split=reconciler.reconcile(List.of(new PhysicalNetworkReconciler.Component<>(owner,"Unused",List.of("a")),new PhysicalNetworkReconciler.Component<>(owner,"Unused",List.of("c"))),Set.of("a","b","c"));
        check(split.get("a").equals(fieldId) && !split.get("a").equals(split.get("c")),"observed removal splits physical network");
        var merged=reconciler.reconcile(List.of(new PhysicalNetworkReconciler.Component<>(owner,"Unused",List.of("a","c"))),Set.of("a","c"));
        check(merged.get("a").equals(merged.get("c")) && physicalDirectory.snapshot().size()==1,"reconnection merges physical network");
        var random=new Random(42);
        for(int i=0;i<1000;i++) {
            int[] capacities=random.ints(20,0,100000).toArray();long demand=random.nextInt(2000000);
            int[] allocation=ProportionalEnergyAllocator.allocate(demand,capacities);
            check(Arrays.stream(allocation).asLongStream().sum()==Math.min(demand,Arrays.stream(capacities).asLongStream().sum()),"allocation conserves energy");
            for(int k=0;k<capacities.length;k++) check(allocation[k]>=0 && allocation[k]<=capacities[k],"allocation bounded");
        }
        var playback=new TutorialPlaybackController(new double[]{2,3},0);playback.advance(2.5);
        check(playback.sceneIndex()==1 && playback.elapsedSeconds()==.5,"chapter transition");
        playback.setPaused(true);playback.onFrame(100);playback.onFrame(1_000_000_100);check(playback.elapsedSeconds()==.5,"pause freezes wall clock");
        playback.seek(50);check(playback.elapsedSeconds()==3,"seek clamps");playback.togglePaused();check(!playback.paused() && playback.elapsedSeconds()==0,"replay final scene");
        playback.advance(100);check(playback.paused() && playback.progress()==1,"end stops");
        rejects(() -> new TutorialPlaybackController(new double[]{Double.NaN},0));
        var progress=new TutorialProgress();check(!progress.completed("test:lesson",1),"new tutorials are not complete");
        check(AnimationMath.wave(0,0,1,1,10)==1 && AnimationMath.wave(0,10,1,1,10)==0,"impact lifetime");
        for(int style=0;style<4;style++) for(int formation=0;formation<3;formation++) {
            var strokes=new AtomicInteger();
            com.zeromods.core.animation.EnergySurface.render(-10,10,0,5,100,10,0,2,0x9933FF,0xFF33CC,style,formation,.5f,
                (x1,y1,x2,y2,w,c,a)-> {
                    check(Float.isFinite(x1+y1+x2+y2+w+a) && a>=0 && a<=1,"surface vertices and alpha are finite");strokes.incrementAndGet();
                });
            check(strokes.get()<10000,"maximum-size field surface has bounded geometry");
        }
        System.out.println("Core contracts passed: networks, permissions, snapshots, filters, settings, energy, GUI transforms, and tutorial playback.");
    }
}
