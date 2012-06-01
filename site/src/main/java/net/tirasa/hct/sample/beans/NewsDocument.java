package net.tirasa.hct.sample.beans;

import java.util.Calendar;

import org.hippoecm.hst.content.beans.Node;
import org.hippoecm.hst.content.beans.standard.HippoHtml;
import org.hippoecm.hst.content.beans.standard.HippoGalleryImageSetBean;

@Node(jcrType="hctsite:newsdocument")
public class NewsDocument extends BaseDocument{

    public Calendar getDate() {
        return getProperty("hctsite:date");
    }

    public HippoHtml getHtml(){
        return getHippoHtml("hctsite:body");    
    }

    /**
     * Get the imageset of the newspage
     *
     * @return the imageset of the newspage
     */
    public HippoGalleryImageSetBean getImage() {
        return getLinkedBean("hctsite:image", HippoGalleryImageSetBean.class);
    }

    public String getSummary() {
        return getProperty("hctsite:summary");
    }

    public String getTitle() {
        return getProperty("hctsite:title");
    }

}
