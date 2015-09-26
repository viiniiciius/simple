package br.com.vitulus.simple.validator;

import br.com.vitulus.simple.validator.tipo.StringValidatorType;

public class StringSizeValidator extends AbstractValidator implements StringValidatorType{

	private int max;
	private int min;
		
	public StringSizeValidator(Validator validator, int max,int min) {
		super(validator, validator.getValue());
		this.max = max;
		this.min = min;
	}
	
	public StringSizeValidator(String value, int max,int min) {
		super(null, value);
		this.max = max;
		this.min = min;
	}
	
	public String getValue(){
		return super.getValue().toString();
	}
	
	@Override
	protected boolean validate() {
		if(getValue().length() > max){
			setErrorMsg("Valor muito longo : Máximo permitido = " + max);
			return false;
		}else if(getValue().length() < min){
			setErrorMsg("Valor muito curto : Mínimo exigido = " + min);
			return false;
		}
		return true;
	}

}