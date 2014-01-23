package edu.upc.fib.roadtriptest;

public class ContactItemRow {
	
	private int _id;
	private String _name;
	private double _cost;
	private boolean _check;
	
	public ContactItemRow(){
		_id = -1;
		_name = "";
		_cost = 0;
		_check = false;
	}
	
	public ContactItemRow(int id, String name, double cost, boolean check){
		_id = id;
		_name = name;
		_cost = cost;
		_check = check;
	}
	
	public int getId(){
		return _id;
	}
	
	public String getName(){
		return _name;
	}
	
	public double getCost(){
		return _cost;
	}
	
	public boolean getCheck(){
		return _check;
	}
	
	public void setCost(double cost){
		_cost = cost;
	}
	
	public void setCheck(boolean check){
		_check = check;
	}
	

}
