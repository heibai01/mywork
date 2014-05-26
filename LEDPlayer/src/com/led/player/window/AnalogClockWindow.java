package com.led.player.window;

import android.graphics.Color;
/**
 * 模拟时钟属性封装。
 * @author 1231
 *
 */
public class AnalogClockWindow {

	private int StartX = 0;
	private int StartY = 0;
	private int Width;
	private int Heiht;
	private int BorderType;
	private int BgClr = Color.BLACK;
	private String BgWord;
	private Boolean ShowDate = true;
	private Boolean ShowWeek = true;
	private TextViewAttr textviewattr = new TextViewAttr();
	
	public void setStartX(int par){
		this.StartX = par;
	}
	public void setStartY(int par){
		this.StartY = par;
	}
	public void setWidth(int par){
		this.Width = par;
	}
	public void setHeiht(int par){
		this.Heiht = par;
	}
	public void setBorderType(int par){
		this.BorderType = par;
	}
	public void setBgClr(int par){
		this.BgClr = par;
	}
	public void setBgWord(String word){
		BgWord = word;
	}
	public void setShowDate(Boolean showdate){
		ShowDate = showdate;
	}
	public void setShowWeek(Boolean showweek){
		ShowWeek = showweek;
	}
	public void setWordClr(int clr){
		this.textviewattr.setWordClr(clr);
	}
	public void setFixedWordClr(int clr){
		this.textviewattr.setFixedWordClr(clr);
	}
	public void setLanguage(boolean language){
		this.textviewattr.setLanguage(language);
	}
	public void setContent(String Content){
		this.textviewattr.setContent(Content);
	}
	public void setItalic(int clr){
		this.textviewattr.setItalic(clr);
	}
	public void setUnderline(int clr){
		this.textviewattr.setUnderline(clr);
	}
	public void setFaceName(int i){
		this.textviewattr.setFaceName(i);
	}
	public void setIfHeight(int height){
		this.textviewattr.setHeight(height);
	}
	public void setWeight(int weight){
		this.textviewattr.setWeight(weight);
	}
	
	public TextViewAttr getTextViewAttr(){
		return textviewattr;
	}
	
	public int getStartX(){
		return StartX ;
	}
	public int getStartY(){
		return StartY;
	}
	public int getWidth(){
		return Width ;
	}
	public int getHeiht(){
		return Heiht ;
	}
	public int getBorderType(){
		return BorderType;
	}
	public int getBgClr(){
		return BgClr;
	}
	public Boolean getShowDate(){
		return ShowDate;
	}
	public Boolean getShowWeek(){
		return ShowWeek;
	}
	public String getBgWord(){
		return BgWord;
	}
}