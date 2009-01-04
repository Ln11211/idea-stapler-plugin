package org.kohsuke.stapler.idea.descriptor;

import com.intellij.psi.PsiElement;
import com.intellij.psi.util.PsiTreeUtil;
import com.intellij.psi.xml.XmlElement;
import com.intellij.psi.xml.XmlFile;
import com.intellij.psi.xml.XmlTag;
import com.intellij.xml.XmlAttributeDescriptor;
import com.intellij.xml.XmlElementDescriptor;
import com.intellij.xml.XmlNSDescriptor;
import com.intellij.xml.impl.dtd.BaseXmlElementDescriptorImpl;
import com.intellij.util.xml.DomManager;

import java.util.HashMap;
import java.util.List;

import org.kohsuke.stapler.idea.dom.model.JellyTag;
import org.kohsuke.stapler.idea.dom.model.AttributeTag;

/**
 * @author Kohsuke Kawaguchi
 */
public class XmlElementDescriptorImpl extends BaseXmlElementDescriptorImpl {
    private final XmlNSDescriptorImpl nsDescriptor;
    private final XmlFile tagFile;

    public XmlElementDescriptorImpl(XmlNSDescriptorImpl nsDescriptor, XmlFile tagFile) {
        this.nsDescriptor = nsDescriptor;
        this.tagFile = tagFile;
    }

    protected XmlElementDescriptor[] doCollectXmlDescriptors(XmlTag xmlTag) {
        return new XmlElementDescriptor[0];
    }

    protected XmlAttributeDescriptor[] collectAttributeDescriptors(XmlTag xmlTag) {
        JellyTag tag = DomManager.getDomManager(tagFile.getProject()).getFileElement(tagFile, JellyTag.class).getRootElement();
        List<AttributeTag> atts = tag.getDocumentation().getAttributes();
        XmlAttributeDescriptor[] descriptors = new XmlAttributeDescriptor[atts.size()];
        int i=0;
        for (AttributeTag a : atts) {
            descriptors[i++] = new XmlAttributeDescriptorImpl(this,a);
        }
        return descriptors;
    }

    protected HashMap<String, XmlAttributeDescriptor> collectAttributeDescriptorsMap(XmlTag xmlTag) {
        HashMap<String, XmlAttributeDescriptor> r = new HashMap<String, XmlAttributeDescriptor>();
        for (XmlAttributeDescriptor a : getAttributesDescriptors(xmlTag))
            r.put(a.getName(xmlTag),a);
        return r;
    }

    protected HashMap<String, XmlElementDescriptor> collectElementDescriptorsMap(XmlTag xmlTag) {
        HashMap<String, XmlElementDescriptor> r = new HashMap<String, XmlElementDescriptor>();
        for (XmlElementDescriptor e : getElementsDescriptors(xmlTag))
            r.put(e.getName(xmlTag),e);
        return r;
    }

    public String getQualifiedName() {
        // TODO: how am I supposed to figure out the prefix?
        return getName();
    }

    public String getDefaultName() {
        return getQualifiedName();
    }

    public XmlNSDescriptor getNSDescriptor() {
        return nsDescriptor;
    }

    public int getContentType() {
        // TODO
        return CONTENT_TYPE_MIXED;
    }

    public PsiElement getDeclaration() {
        return tagFile;
    }

    public String getName(PsiElement context) {
        String n = getName();
        if (context instanceof XmlElement) {
            XmlTag xmltag = PsiTreeUtil.getParentOfType(context, XmlTag.class, false);
            if (xmltag != null) {
                String prefix = xmltag.getPrefixByNamespace(nsDescriptor.uri);
                if (prefix != null && prefix.length() > 0)
                    return prefix + ':' + n;
            }
        }
        return n;
    }

    public String getName() {
        return tagFile.getName();
    }

    public void init(PsiElement element) {
    }

    public Object[] getDependences() {
        return new Object[] {nsDescriptor,tagFile};
    }

}