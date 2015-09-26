package br.com.vitulus.simple.validator;

import br.com.vitulus.simple.validator.tipo.ObjectValidatorType;

public class NullValidator extends AbstractValidator implements ObjectValidatorType{

	public NullValidator(Object value) {
		super(null , value);
	}
	
	public NullValidator(Validator next) {
		super(next , next.getValue());
	}
	
	@Override
	protected boolean validate() {
		if(getValue() == null){
			setErrorMsg("O valor está nulo.");
			return false;
		}
		return true;
	}

}