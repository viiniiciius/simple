package br.com.vitulus.simple.validator;

import java.util.List;

import br.com.vitulus.simple.validator.tipo.ObjectValidatorType;

public class DomainValidator<T> extends AbstractValidator implements ObjectValidatorType{
	
	private List<T> domain;
	
	public DomainValidator(Validator next,List<T> domain,T value) {
		super(next, value);
		this.domain = domain;
	}
	
	public DomainValidator(List<T> domain,T value) {
		this(null,domain,value);
	}	

	@Override
	protected boolean validate() {
		if(!domain.contains(getValue())){
			setErrorMsg("Valor inesperado.");
			return false;
		}
		return true;
	}

}