package br.com.vitulus.simple.validator;

import java.util.regex.Pattern;

import br.com.vitulus.simple.validator.tipo.NumericValidatorType;

public class NumberValidator extends AbstractValidator implements NumericValidatorType{

	protected static Pattern numberPattern;
	private EmptyValidator   emptyValidator;
	
	static{
		numberPattern = Pattern.compile("^(\\d|-){1}\\d*\\.?\\d*$");
	}	
	
	public NumberValidator(Object value) {
		super(null, value);
		emptyValidator = new EmptyValidator(value,true);
	}
	
	public NumberValidator(Validator next) {
		super(next, next.getValue());
		emptyValidator = new EmptyValidator("",true);
	}

	@Override
	public void setValue(Object value) {
		super.setValue(value);
		emptyValidator.setValue(value);
	}
	
	public Double getValue(){
		return Double.parseDouble(super.getValue().toString());
	}
	
	protected boolean validate(){
		String strValue = super.getValue().toString();
		emptyValidator.setValue(strValue);
		if(!emptyValidator.isValid()){
			setErrorMsg(emptyValidator.getErrorMessage());
			setSource(emptyValidator);
			return false;
		}
		if(!numberPattern.matcher(strValue).matches()){
			setErrorMsg("Não é um número.");
			return false;
		}
		return true;
	}

	
}