package net.tirasa.hct.sample.beans;

import org.hippoecm.hst.content.beans.Node;
import org.hippoecm.hst.content.beans.standard.HippoHtml;

@Node(jcrType="hctsite:textdocument")
public class TextDocument extends BaseDocument{
    
    public HippoHtml getHtml(){
        return getHippoHtml("hctsite:body");    
    }

    public String getSummary() {
        return getProperty("hctsite:summary");
    }
 
    public String getTitle() {
        return getProperty("hctsite:title");
    }

}
