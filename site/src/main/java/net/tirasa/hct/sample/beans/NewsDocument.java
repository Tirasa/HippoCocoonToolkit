package net.tirasa.hct.sample.beans;

import java.util.Calendar;

import org.hippoecm.hst.content.beans.Node;
import org.hippoecm.hst.content.beans.standard.HippoHtml;
import org.hippoecm.hst.content.beans.standard.HippoGalleryImageSetBean;

@Node(jcrType="sample:newsdocument")
public class NewsDocument extends BaseDocument{

    public Calendar getDate() {
        return getProperty("sample:date");
    }

    public HippoHtml getHtml(){
        return getHippoHtml("sample:body");    
    }

    /**
     * Get the imageset of the newspage
     *
     * @return the imageset of the newspage
     */
    public HippoGalleryImageSetBean getImage() {
        return getLinkedBean("sample:image", HippoGalleryImageSetBean.class);
    }

    public String getSummary() {
        return getProperty("sample:summary");
    }

    public String getTitle() {
        return getProperty("sample:title");
    }

}
