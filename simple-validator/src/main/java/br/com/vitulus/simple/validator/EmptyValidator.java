package br.com.vitulus.simple.validator;

import br.com.vitulus.simple.validator.tipo.StringValidatorType;

public class EmptyValidator extends AbstractValidator implements StringValidatorType{

	private boolean trimmed;
	
	public EmptyValidator(Object value) {
		this(value,true);
	}
	
	public EmptyValidator(Validator next) {
		this(next ,true);
	}
	
	public EmptyValidator(Object value,boolean trimmed) {
		super(null , value);
		this.trimmed = trimmed;
	}
	
	public EmptyValidator(Validator next,boolean trimmed) {
		super(next , next.getValue());
		this.trimmed = trimmed;
	}

	public String getValue(){
		return super.getValue().toString();
	}
	
	@Override
	protected boolean validate() {
		if(getValue().isEmpty()){
			setErrorMsg("O valor está vazio.");
			return false;
		}
		if(trimmed & getValue().trim().isEmpty()){
			setErrorMsg("O valor tem conteúdo vazio.");
			return false;
		}
		return true;
	}
	


}