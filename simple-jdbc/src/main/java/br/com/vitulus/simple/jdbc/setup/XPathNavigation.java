package br.com.vitulus.simple.jdbc.setup;

import java.util.ArrayList;
import java.util.List;

import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathExpression;
import javax.xml.xpath.XPathExpressionException;

import org.w3c.dom.Attr;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

/**
 * <p>Auxilia na recuperação e alteração dos resultados de uma consulta XPath.</p>
 * <p>Fornece métodos convenientes que estruturam os resultados da consulta.</p>
 * <p>@see <a href="http://www.w3schools.com/xpath/default.asp">XPath Tutorial</a></p>
 * <p>@see <a href="http://www.w3schools.com/dom/default.asp">XML DOM Tutorial</a></p>
 * @author vinicius.rodrigues
 */
public class XPathNavigation {

    private NodeList             expressionResult;
    private XPathExpression      expression;
    private Node                 dom;

    /**
     * Constroi a classe utilitaria de navegação em um NodeList
     * @param expression A expressão XPath que será utilizada
     * @param dom O DOM alvo das consultas
     * @throws XPathExpressionException Em caso de erros de sintaxe na expressão xpath
     */
    public XPathNavigation(XPathExpression expression,Node dom) throws XPathExpressionException{
        this.expression = expression;
        this.dom = dom;
    }

    /**
     * Objeto DOM alvo das consultas
     * @return DOM alvo
     */
    public Node getDom() {
        return dom;
    }

    /**
     * Seta o objeto DOM alvo
     * @param dom DOM alvo
     */
    public void setDom(Node dom) {
        this.dom = dom;
    }

    /**
     * Consulta Xpath
     * @return A consulta XPath que gerou os resultados
     */
    public XPathExpression getExpression() {
        return expression;
    }

    /**
     * Seta a consulta a ser realizada.
     * @param expression Consulta XPah
     */
    public void setExpression(XPathExpression expression) {
        this.expression = expression;
    }

    /**
     * Executa a consulta Xpath no DOM alvo
     * @throws XPathExpressionException Caso erro de consuta
     */
    protected void evalute() throws XPathExpressionException {
        Object result = evalute(this.expression , this.dom);
        if (result != null) {
            this.expressionResult = (NodeList) result;
        }
    }

    /**
     * Executa a consulta XPath no alvo DOM
     * @param expression A expressão da consulta
     * @param dom O objeto DOM alvo
     * @return Uma instancia de 'NodeSet' , que é uma lista de objetos DOM ,podem ser 'Attribute' ou 'Element' depende da consulta.
     * @throws XPathExpressionException
     */
    public Object evalute(XPathExpression expression,Node dom) throws XPathExpressionException{
        return expression.evaluate(dom, XPathConstants.NODESET);
    }

    @SuppressWarnings("unchecked")
    private <T> T getGenericElement(NodeList result,int index,Class<T> classe){
        Node node = null;
        if(index < result.getLength() && (node = result.item(index)) != null && classe.isAssignableFrom(node.getClass())){
            return (T)node;
        }
        return null;
    }

    /**
     * Retorna um elemento especifico do resultado da consulta presupondo que ele seja um 'Element'.
     * @param result O resultado da consulta Xpath
     * @param index  O indice do elemento a ser retornado.
     * @return
     */
    public Element getElement(NodeList result,int index){
        return getGenericElement(result, index, Element.class);
    }

    /**
     * Retorna um atributo especifico do resultado da consulta presupondo que ele seja um 'Attribute'.
     * @param result O resultado da consulta Xpath
     * @param index  O indice do elemento a ser retornado.
     * @return
     */
    public Attr getAttribute(NodeList result,int index){
        return getGenericElement(result, index, Attr.class);
    }

    /**
     * Adimitindo que a consulta XPath deveria ter apenas um resultado do tipo 'Element' traz o primeiro resultado da consulta.
     * @return O primeiro (unico) DOM 'Element' do resultado ou null caso não haja resultados
     */
    public Element getUniqueElement(){
        return getElement(getExpressionResult() , 0);
    }

    /**
     * Adimitindo que a consulta XPath deveria ter apenas um resultado do tipo 'Attribute' traz o primeiro resultado da consulta.
     * @return O primeiro (unico) DOM 'Attribute' do resultado ou null caso não haja resultados
     */
    public Attr getUniqueAttribute(){
        return getAttribute(getExpressionResult() , 0);
    }

    /**
     * Uma lista com todos os resultados da consulta admitindo-se que são DOMs de 'Element'
     * @return Lista de DOMs 'Element' ou uma Lista vazia caso não haja resultados
     */
    public List<Element> getElements(){
        List<Element> list = new ArrayList<Element>();
        for(int i = 0; i < getExpressionResult().getLength() ; i++){
            Element element = getElement(this.getExpressionResult() , i);
            if(element != null){
                list.add(element);
            }
        }
        return list;
    }

    /**
     * Uma lista com todos os resultados da consulta admitindo-se que são DOMs de 'Attribute'
     * @return Lista de DOMs 'Attribute' ou uma Lista vazia caso não haja resultados
     */
    public List<Attr> getAttributes(){
        List<Attr> list = new ArrayList<Attr>();
        for(int i = 0; i < getExpressionResult().getLength() ; i++){
            Attr attribute = getAttribute(this.getExpressionResult() , i);
            if(attribute != null){
                list.add(attribute);
            }
        }
        return list;
    }

    /**
     * Admitindo que a consulta tem um unico resultado.Pega o conteúdo de texto do Elemento.Desde que seja um 'Element'
     * @return Uma String com o a representação texto do valor do elemento ou null caso não haja elemento no resultado da consulta.
     */
    public String getElementValue(){
        Element element = getElement(this.getExpressionResult() , 0);
        if(element != null){
            return element.getTextContent();
        }
        return null;
    }

    /**
     * Admitindo que a consulta tem um unico resultado.Pega o conteúdo de texto do Atributo.Desde que seja um 'Attribute'
     * @return Uma String com o a representação texto do valor do atributo ou null caso não haja atributo no resultado da consulta..
     */
    public String getAttributeValue(){
        Attr attribute = getAttribute(this.getExpressionResult() , 0);
        if(attribute != null){
            return attribute.getNodeValue();
        }
        return null;
    }

    /**
     * Pega o conteúdo de texto de cada Elemento e estrutura em uma lista.Desde que sejam instancias 'Element'
     * @return Uma Lista de Strings com o a representação texto de cada elemento.
     */
    public List<String> getElementsValues(){
        List<String> list = new ArrayList<String>();
        for(Element element : getElements()){
            list.add(element.getTextContent());
        }
        return list;
    }

    /**
     * Pega o conteúdo de texto de cada Atributo e estrutura em uma lista.Desde que sejam instancias 'Attribute'
     * @return Uma Lista de Strings com o a representação texto de cada elemento.
     */
    public List<String> getAttributesValues(){
        List<String> list = new ArrayList<String>();
        for(Attr attribute : getAttributes()){
            list.add(attribute.getNodeValue());
        }
        return list;
    }

    /**
     * Adiciona um filho ao elemento unico (primeiro elemento) do resultado da consulta.
     * @param element O elemento que deve ser inserido
     * @return Verdadeiro caso a inserção tenha sido feita. Falso em caso contrario , provavelmente porque a consulta não pussia resultasdo.
     */
    public boolean addChildElement(Element element){
        return addChildElement(element , getUniqueElement());
    }

    /**
     * Adiciona um atributo ao elemento unico (primeiro elemento) do resultado da consulta.
     * @param element O elemento que deve ser inserido
     * @return Verdadeiro caso a inserção tenha sido feita. Falso em caso contrario , provavelmente porque a consulta não pussia resultasdo.
     */
    public boolean addChildAttribute(Attr attribute){
        Element parent = attribute.getOwnerElement();
        return addChildAttribute(attribute , parent);
    }

    /**
     * Adiciona um elemento DOM (filho) em outro elemento DOM (pai) .
     * @param element O elemento filho a ser adicionado
     * @param parent O elemento pai que receberá o filho
     * @return Verdadeiro caso o elemento pai e filho não sejam referencias nulas.
     */
    public boolean addChildElement(Element element,Element parent){
        if(element != null && parent != null){
            parent.appendChild(element);
            return true;
        }
        return false;
    }

    /**
     * Adiciona um atributo em elemento .
     * @param attribute Atributo a ser inserido.
     * @param parent Elemento que recebera o atributo.
     * @return Verdadeiro caso o atributo e o elemento não sejam referencias nulas.
     */
    public boolean addChildAttribute(Attr attribute,Element parent){
        if(parent != null && parent != null){
            parent.appendChild(parent);
            return true;
        }
        return false;

    }

    /**
     * Seta o conteúdo de texto no (primeiro,unico) elemento do resultado.
     * @param value O valor texto a ser atribuido.
     * @return Verdadeiro caso o a consulta tenha um resultado valido ,
     *  ou seja que o primeiro elemento não seja nulo e possa ter o valor atribuido.
     */
    public boolean setElementValue(String value){
        Element element = getElement(this.getExpressionResult() , 0);
        if(element != null){
            element.setTextContent(value);
            return true;
        }
        return false;
    }

    /**
     * Seta o conteúdo de texto no (primeiro,unico) atributo do resultado
     * @param value O valor texto a ser atribuido.
     * @return Verdadeiro caso o a consulta tenha um resultado valido ,
     *  ou seja que o primeiro atributo não seja nulo e possa ter o valor atribuido.
     */
    public boolean setAttributeValue(String value){
        Attr attribute = getUniqueAttribute();
        if(attribute != null){
            attribute.setTextContent(value);
            return true;
        }
        return false;
    }

    /**
     * Executa a consulta Xpath e devolve o resultado
     * @return O resultado original da consulta sem qualquer manipulação ou null caso a consulta seha nula.
     */
    public NodeList getExpressionResult() {
        if(expressionResult == null){
            try {
                evalute();
            } catch (XPathExpressionException ex) {
                ex.printStackTrace();
            }
        }
        return expressionResult;
    }
}