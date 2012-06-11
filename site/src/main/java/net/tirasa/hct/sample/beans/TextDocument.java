package net.tirasa.hct.sample.beans;

import org.hippoecm.hst.content.beans.Node;
import org.hippoecm.hst.content.beans.standard.HippoHtml;

@Node(jcrType="sample:textdocument")
public class TextDocument extends BaseDocument{
    
    public HippoHtml getHtml(){
        return getHippoHtml("sample:body");    
    }

    public String getSummary() {
        return getProperty("sample:summary");
    }
 
    public String getTitle() {
        return getProperty("sample:title");
    }

}
