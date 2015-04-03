package imagemap;

import java.util.*;
import java.awt.*;

/**
 * Title:        ImageMap
 * Description:  client side imagemap creator
 * Copyright:    Copyright (c) 2001
 * Company:      webdesign-pirna.de
 * @author Andreas Tetzl
 * @version 1.0
 */

public class HTMLParser {

  String html;
  int parseIndex;

  public HTMLParser(String html) {
    this.html=new String(html);
    parseIndex=0;
  }

  public String getImageSrc() {
    parseIndex=0;
    HTMLTag tag;
    while ((tag=nextTag())!=null) {
      if (tag.getTagType().compareToIgnoreCase("img")==0 && tag.getArgumentValue("usemap") != null) {
        return tag.getArgumentValue("src");
      }
    }
    return "";
  }

  public String getMapName() {
    parseIndex=0;
    HTMLTag tag;
    while ((tag=nextTag())!=null) {
      if (tag.getTagType().compareToIgnoreCase("map")==0 && tag.getArgumentValue("name") != null) {
        return tag.getArgumentValue("name");
      }
    }
    return "";
  }  
  
  public void createShapeList(ShapeList sl) {
    sl.clear();
    HTMLTag tag;

    parseIndex=0;
    while ((tag=nextTag())!=null) {
      Shape sh=null;
      if (tag.getTagType().compareToIgnoreCase("area")==0 && tag.getArgumentValue("shape").compareToIgnoreCase("rect")==0) {
        StringTokenizer st = new StringTokenizer(tag.getArgumentValue("coords"), ",");
        int x1=0, y1=0, x2=0, y2=0;
        if (st.hasMoreTokens()) x1=new Integer(st.nextToken().trim()).intValue();
        if (st.hasMoreTokens()) y1=new Integer(st.nextToken().trim()).intValue();
        if (st.hasMoreTokens()) x2=new Integer(st.nextToken().trim()).intValue();
        if (st.hasMoreTokens()) y2=new Integer(st.nextToken().trim()).intValue();
        sh = new Shape(x1,y1,x2,y2);
      } else if (tag.getTagType().compareToIgnoreCase("area")==0 && tag.getArgumentValue("shape").compareToIgnoreCase("circle")==0) {
        StringTokenizer st = new StringTokenizer(tag.getArgumentValue("coords"), ",");
        int x=0, y=0, r=0;
        if (st.hasMoreTokens()) x=new Integer(st.nextToken().trim()).intValue();
        if (st.hasMoreTokens()) y=new Integer(st.nextToken().trim()).intValue();
        if (st.hasMoreTokens()) r=new Integer(st.nextToken().trim()).intValue();
        sh=new Shape(new Point(x,y), r);
      } else if (tag.getTagType().compareToIgnoreCase("area")==0 && (tag.getArgumentValue("shape").compareToIgnoreCase("poly")==0 || tag.getArgumentValue("shape").compareToIgnoreCase("polygon")==0)) {
        StringTokenizer st = new StringTokenizer(tag.getArgumentValue("coords"), ",");
        Polygon poly=new Polygon();
        while (st.hasMoreTokens()) {
          int x=0, y=0;
          x=new Integer(st.nextToken().trim()).intValue();
          if (st.hasMoreTokens()) y=new Integer(st.nextToken().trim()).intValue();
          poly.addPoint(x,y);
        }
        sh=new Shape(poly);
      }

      if (sh!=null) {
        sh.set_href(tag.getArgumentValue("href"));
        sh.set_alt(tag.getArgumentValue("alt"));
        sh.set_onMouseOver(tag.getArgumentValue("onMouseOver"));
        sh.set_onMouseOut(tag.getArgumentValue("onMouseOut"));
        sh.set_onClick(tag.getArgumentValue("onClick"));
        sl.add_shape(sh);
      }
    }
  }


  public HTMLTag nextTag() {
    String newtag;
    int i,j;
    for (i=parseIndex; i<html.length(); i++) {
      if (html.charAt(i)=='<') {
        newtag=new String();
        for (j=i; j<html.length() && html.charAt(j)!='>'; j++) {
          newtag+=html.substring(j, j+1);
        }
        newtag+=">";
        parseIndex=j+1;
        return new HTMLTag(newtag);
      }
    }
    return null;

  }

}



