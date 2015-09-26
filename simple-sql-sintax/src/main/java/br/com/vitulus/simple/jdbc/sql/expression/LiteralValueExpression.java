package br.com.vitulus.simple.jdbc.sql.expression;

import java.io.IOException;


public class LiteralValueExpression implements SqlExpression{

	private Object 				value;
	private boolean				preparedValue;
	private LiteralListener 	listener;
	
	public LiteralValueExpression() {
	}
	
	public LiteralValueExpression(Object value,boolean preparedValue) {
		this.value = value;
		this.preparedValue = preparedValue;
	}
	
	public LiteralValueExpression(Object value) {
		this(value,true);
	}
	
	public String getLiteralString(){
		if(value == null){
			return "NULL";
		}
		return value.toString();
	}
	
	public Number getLiteralNumeric(){
		if(value == null){
			return 0;
		}
		return (Number) value;
	}
	
	public byte[] getLiteralBlob(){
		if(value == null){
			return null;
		}
		return (byte[]) value;
	}	
	
	public LiteralListener getListener() {
		return listener;
	}

	public void setListener(LiteralListener listener) {
		this.listener = listener;
	}

	@Override
	public void append(Appendable p) throws IOException {
		if(listener != null){
			listener.onAppend(value);
		}		
		p.append(preparedValue ? "?" : toString());
	}
	
	@Override
	public String toString() {
		return getLiteralString();
	}
	
	public static interface LiteralListener{
		void onAppend(Object value);
	}	

	public static interface LiteralFactory{
		public LiteralValueExpression getLiteral(Object value);
	}
	

}