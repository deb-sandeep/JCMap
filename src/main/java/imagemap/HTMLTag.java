package imagemap;

/**
 * Title:        ImageMap
 * Description:  client side imagemap creator
 * Copyright:    Copyright (c) 2001
 * Company:      webdesign-pirna.de
 * @author Andreas Tetzl
 * @version 1.0
 */

class HTMLTag {
  String tag;
  String tag_lc;

  public HTMLTag(String tag) {
    this.tag=tag;
    tag_lc=tag.toLowerCase();
  }

  /**
   * returns the value of the tag argument arg
   * Example:
   * tag is: <img src="xxx" border=0>
   * getArgumentValue("src") returns String xxx
   */
  public String getArgumentValue(String arg) {
    if (arg.length()>tag.length()+5) return null;
    String val = new String();
    int i=tag_lc.indexOf(arg.toLowerCase());
    if (i<0) return null;
    i=tag.indexOf("=", i);
    if (i<0) return null;
    i++;

    for (;i<tag.length() && tag.charAt(i)==' '; i++);
    if (tag.charAt(i)=='\"')  // argument value enclosed in ""
      for (i++; i<tag.length() && tag.charAt(i)!='\"'; i++)
        val+=tag.substring(i,i+1);
    else
      for (; i<tag.length() && tag.charAt(i)!=' ' && tag.charAt(i)!='>'; i++)
        val+=tag.substring(i,i+1);

    return val;
  }

  public String getTagType() {
    int i=tag.indexOf(" ");
    if (i<0) i=tag.indexOf(">");
    if (i<0) i=tag.length()-1;
    return tag.substring(1, i).toLowerCase();
  }
}