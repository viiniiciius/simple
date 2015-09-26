package br.com.vitulus.simple.validator;

import java.util.regex.Pattern;

import br.com.vitulus.simple.validator.tipo.StringValidatorType;

public class CnpjValidator extends AbstractValidator implements
		StringValidatorType {

	protected static Pattern replaceSintaxPattern;

	static {
		replaceSintaxPattern = Pattern.compile("[\\.\\-\\s]");
	}

	public CnpjValidator(String value) {
		this(null, value);
	}

	public CnpjValidator(Validator next, String value) {
		super(next, value);
	}

	@Override
	public String getValue() {
		return super.getValue().toString();
	}

	@Override
	protected boolean validate() {
		String cnpj = replaceSintaxPattern.matcher(getValue()).replaceAll("");
		int soma = 0, dig;
		String cnpj_calc = cnpj.substring(0, 12);
		char[] chr_cnpj = cnpj.toCharArray();
		for (int i = 0; i < 4; i++){
			if (chr_cnpj[i] - 48 >= 0 && chr_cnpj[i] - 48 <= 9){
				soma += (chr_cnpj[i] - 48) * (6 - (i + 1));
			}
		}
		for (int i = 0; i < 8; i++){
			if (chr_cnpj[i + 4] - 48 >= 0 && chr_cnpj[i + 4] - 48 <= 9){
				soma += (chr_cnpj[i + 4] - 48) * (10 - (i + 1));
			}
		}
		dig = 11 - (soma % 11);
		cnpj_calc += (dig == 10 || dig == 11) ? "0" : Integer.toString(dig);
		soma = 0;
		for (int i = 0; i < 5; i++){
			if (chr_cnpj[i] - 48 >= 0 && chr_cnpj[i] - 48 <= 9){
				soma += (chr_cnpj[i] - 48) * (7 - (i + 1));
			}
		}
		for (int i = 0; i < 8; i++){
			if (chr_cnpj[i + 5] - 48 >= 0 && chr_cnpj[i + 5] - 48 <= 9){
				soma += (chr_cnpj[i + 5] - 48) * (10 - (i + 1));
			}
		}
		dig = 11 - (soma % 11);
		cnpj_calc += (dig == 10 || dig == 11) ? "0" : Integer.toString(dig);
		boolean valid = cnpj.equals(cnpj_calc);
		if (!valid) {
			setErrorMsg("CNPJ Inválido.");
		}
		return valid;
	}

}