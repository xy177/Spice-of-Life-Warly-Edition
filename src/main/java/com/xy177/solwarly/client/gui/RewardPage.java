// Warly reward timeline, implemented by xy177, 2026-10-27; LGPL-2.1.
// Uses the Apple Pie-derived Page and UI elements; geometry follows Warly's player-facing design.
package com.xy177.solwarly.client.gui;
import com.xy177.solwarly.client.gui.elements.*;
import com.xy177.solwarly.item.SOLWarlyItems;
import net.minecraft.client.resources.language.I18n;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.ItemStack;
import java.awt.*;
import java.util.Map;
import java.util.SortedMap;
import static com.xy177.solwarly.lib.DisplayNumbers.format;

final class RewardPage extends Page {
    RewardPage(Rectangle frame,String title) { super(frame,title); }
    void addProgress(int row,String title,double score,SortedMap<Double,String> rewards,Map<Double,Double> hearts,boolean diversity) {
        int center=getCenterX(), y=getMinY()+row;
        if(row>60)label(title,contentWidth,center,y-38,false,null,FoodBookScreen.fullBlack);
        if(rewards.isEmpty()) { label(I18n.get("solwarly.book.no_rewards"),contentWidth,center,y,false,null,FoodBookScreen.fullBlack);return; }
        RewardProgress progress=new RewardProgress(score,rewards.keySet());
        double previous=progress.currentTier==null?0:progress.currentTier;
        boolean complete=progress.next==null;
        int start=center-24, end=center+24;
        double fraction=complete?(score>progress.maximum?1:0):Math.max(0,Math.min(1,(score-previous)/(progress.next-previous)));
        int position=start+(int)Math.round((end-start)*fraction);
        String kind=diversity?"diversity":"experience";
        String currentReward=progress.currentTier==null?I18n.get("solwarly.book.no_reward"):rewards.get(progress.currentTier);
        if(!diversity)currentReward=I18n.get("solwarly.book.total_hearts",format(hearts.getOrDefault(previous,0.0)));
        String current=tip(kind,"current",score,currentReward);
        String highest=tip(kind,"maximum",progress.maximum,rewards.get(progress.maximum));
        String remaining=complete?I18n.get("solwarly.book.max_rewards"):I18n.get("solwarly.book."+kind+".remaining",format(progress.next-score));
        UIItemStack icon=new UIItemStack(new ItemStack(SOLWarlyItems.FOOD_BOOK.get()));
        icon.setCenterX(center-49);icon.setCenterY(y);icon.tooltip=remaining;children.add(icon);
        line(center-36,position,y,current+"\n"+remaining,FoodBookScreen.fullBlack);
        if(!complete)line(position,center+36,y,current+"\n"+remaining,FoodBookScreen.leastBlack);
        if(progress.currentTier==null) {
            String zero=tip(kind,"previous",0,I18n.get("solwarly.book.no_reward"));
            label("0",48,start,y-3,true,zero,FoodBookScreen.fullBlack);
            tick(start,y-3,y,zero,FoodBookScreen.fullBlack);
        } else node(start,y,previous,true,hearts.getOrDefault(previous,0.0),diversity,
            complete?highest:tip(kind,"previous",previous,rewards.get(previous)));
        if(!complete) {
            node(end,y,progress.next,false,hearts.getOrDefault(progress.next,0.0),diversity,tip(kind,"next",progress.next,rewards.get(progress.next)));
            UILabel last=new UILabel(format(progress.maximum),20);last.setMinX(center+39);last.setCenterY(y);
            last.color=FoodBookScreen.leastBlack;last.tooltip=highest;children.add(last);
        }
        tick(position,y,y+6,current,FoodBookScreen.fullBlack);
        label(format(score),48,position,y+9,false,current,FoodBookScreen.fullBlack);
        line(center-47,center+47,y+22,null,FoodBookScreen.leastBlack);
    }
    private String tip(String kind,String stage,double score,String reward) {
        return I18n.get("solwarly.book."+kind+"."+stage+"_score",format(score))+"\n"
            +I18n.get("solwarly.book."+kind+"."+stage+"_reward",diversityReward(kind,reward));
    }
    private String diversityReward(String kind,String reward) { return kind.equals("diversity")?"\n"+reward:reward; }
    private UILabel label(String text,int width,int x,int y,boolean above,String tooltip,Color color) {
        UILabel label=new UILabel(text,width);label.setCenterX(x);if(above)label.setMaxY(y);else label.setMinY(y);
        label.color=color;label.tooltip=tooltip;children.add(label);return label;
    }
    private void line(int from,int to,int y,String tooltip,Color color) {
        UIElement line=UIBox.horizontalLine(from,to,y,color);line.tooltip=tooltip;children.add(line);
    }
    private void tick(int x,int from,int to,String tooltip,Color color) {
        UIElement line=UIBox.verticalLine(x,from,to,color);line.tooltip=tooltip;children.add(line);
    }
    private void node(int x,int y,double score,boolean earned,double hearts,boolean diversity,String tooltip) {
        Color color=earned?FoodBookScreen.fullBlack:FoodBookScreen.lessBlack;
        UILabel value=label(format(score),48,x,y-3,true,tooltip,color);
        tick(x,y-3,y,tooltip,color);
        UIStack symbols=new UIStack();symbols.spacing=1;
        if(diversity)symbols.addChild(new UIItemStack(new ItemStack(Items.EXPERIENCE_BOTTLE)));
        else {
            UIImage image=new UIImage(FoodBookScreen.heartImage);image.setSize(11,11);image.alpha=earned?1:.5f;symbols.addChild(image);
            UILabel amount=new UILabel("×"+format(hearts));amount.color=color;symbols.addChild(amount);
        }
        symbols.setCenterX(x);symbols.setMaxY(value.getMinY()-4);symbols.updateFrames();symbols.tooltip=tooltip;children.add(symbols);
    }
}
