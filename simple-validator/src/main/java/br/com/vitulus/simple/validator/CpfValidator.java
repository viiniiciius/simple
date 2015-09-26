package br.com.vitulus.simple.validator;

import java.util.regex.Pattern;

import br.com.vitulus.simple.validator.tipo.StringValidatorType;

public class CpfValidator extends AbstractValidator implements StringValidatorType{

	protected static Pattern replaceSintaxPattern;
	
	static{
		replaceSintaxPattern = Pattern.compile("[\\.\\-\\s]");
	}
	
	public CpfValidator(String value) {
		this(null, value);
	}
	
	public CpfValidator(Validator next, String value) {
		super(next, value);
	}

	@Override
	public String getValue() {
		return super.getValue().toString();
	}
	
	@Override
	protected boolean validate() {
		String cpf =  replaceSintaxPattern.matcher(getValue()).replaceAll("");
		int     d1, d2;
		int     digito1, digito2, resto;
		int     digitoCPF;
		String  nDigResult;
		d1 = d2 = 0;
		digito1 = digito2 = resto = 0;
		for (int n_Count = 1; n_Count < cpf.length() -1; n_Count++)	{
			digitoCPF = Integer.valueOf (cpf.substring(n_Count -1, n_Count)).intValue();
			d1 = d1 + ( 11 - n_Count ) * digitoCPF;
			d2 = d2 + ( 12 - n_Count ) * digitoCPF;
		}
		resto = (d1 % 11);
		if (resto < 2){
			digito1 = 0;
		}else{
			digito1 = 11 - resto;
		}
		d2 += 2 * digito1;
		resto = (d2 % 11);
		if (resto < 2){
			digito2 = 0;
		}else{
			digito2 = 11 - resto;
		}
		String nDigVerific = cpf.substring (cpf.length()-2, cpf.length());
		nDigResult = String.valueOf(digito1) + String.valueOf(digito2);
		boolean valid = nDigVerific.equals(nDigResult);
		if(!valid){
			setErrorMsg("CPF Inválido.");
		}
		return valid;
		
	}	

}