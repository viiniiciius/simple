package br.com.vitulus.simple.validator;



public abstract class AbstractValidator implements Validator{

	private Validator  next;
	private String     errorMsg;
	private Validator  source;
	private Object     value;

	public AbstractValidator(Validator next, Object value) {
		super();		
		this.value = value;
		queueNext(next);
	}	
	
	private void queueNext(Validator next){
		this.next = next;
		checkAssociation();
	}
	
	@Override
	public Validator concatBegin(Validator next){
		queueNext(next);
		return this;
	}
	
	@Override
	public Validator concat(Validator next){
		if(getNext() == null){
			queueNext(next);
		}else{
			getNext().concat(next);
		}
		return this;
	}
	
	private void checkAssociation(){
		if(next != null){
			Class<?>[] nextTypes = next.getClass().getInterfaces();
			Class<?>[] thisTypes = this.getClass().getInterfaces();
			boolean association = true;
			for(Class<?> forwardType : nextTypes){
				boolean contain = false;
				contain:
				for(Class<?> type : thisTypes){
					contain = contain || type.isAssignableFrom(forwardType);
					if(contain){
						break contain;
					}
				}
				association = association && contain;
				confirmAssociation(association);
			}
		}
	}
	
	private void confirmAssociation(boolean association){
		if(!association){
			throw new RuntimeException("Associação iválida : " +
					this.getClass().getName() + " e " +
					next.getClass().getName() +
					" não são compatíveis");
		}
	}	
	
	public Object getValue() {
		return value;
	}
	
	public void setValue(Object value) {
	    this.value = value;
		if(getNext() != null){
			getNext().setValue(value);
		}
	}

	public Validator getNext() {
		return next;
	}

	public void setNext(Validator next) {
		this.next = next;
	}

	public String getErrorMessage() {
		return errorMsg;
	}

	public void setErrorMsg(String errorMsg) {
		setSource(this);
		this.errorMsg = errorMsg;
	}

	protected abstract boolean validate();
	
	public boolean isValid(){
		return validate() && chain();
	}
	
	protected boolean chain(){
		if(getNext() == null){
			return true;
		}
		boolean valid = getNext().isValid();
		setErrorMsg(getNext().getErrorMessage());
		setSource(getNext().getSource());
		return  valid;
	}

	public Validator getSource() {
		return source;
	}

	public void setSource(Validator source) {
		this.source = source;
	}

	public String getErrorMsg() {
		return errorMsg;
	}
}