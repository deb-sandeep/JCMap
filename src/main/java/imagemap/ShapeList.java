package imagemap;

import java.util.List ;
import java.util.Vector ;

/**
 * Title:        ImageMap
 * Description:  client side imagemap creator
 * Copyright:    Copyright (c) 2001
 * Company:      webdesign-pirna.de
 * @author Andreas Tetzl
 * @version 1.0
 */

public class ShapeList {

  private Vector<Shape> vec = null;
  private Shape foundShape=null;
  private int foundKeyPoint=0;
  public String mapName="MAP";
  
  public ShapeList() {
    vec=new Vector<Shape>();
  }
  
  public List<Shape> getShapes() {
      return this.vec ;
  }

  public void add_shape(Shape s) {
    vec.add(s);
  }

  public Shape get_shape(int i) {
    return (Shape)vec.get(i);
  }

  public void remove_shape(Shape s) {
    vec.remove(s);
  }

  public int size() {
    return vec.size();
  }
  public void clear() {
    vec.clear();
  }
  /**
   * überprüft, ob der Punkt in einem der Shape Objekte ein Anfasspunkt ist und merkt sich
   * das gefundene Objekt.
   * Das Objekt kann später mit  getFoundShape() abgefragt werden.
   * Wird fürs Popup Menu gebraucht, beim öffnen wird diese Methode aufgerufen, bei der
   * Auswahl eines Menupunktes getFoundShape()
   */
  public boolean isShapePointAt(int x, int y) {
    for (int i=0; i<size(); i++) {
      foundKeyPoint=((Shape)vec.get(i)).isKeyPoint(x,y);
      if (foundKeyPoint!=0) {
        foundShape=(Shape)vec.get(i);
        return true;
      }
    }
    return false;
  }

  public boolean isPointInsideShape(int x, int y) {
    for (int i=size()-1; i>=0; i--) {
      if (((Shape)vec.get(i)).inside(x,y)) {
        foundShape=(Shape)vec.get(i);
        foundKeyPoint=0;
        return true;
      }
    }
    return false;
  }

  public Shape getFoundShape() {
    return foundShape;
  }
  public int getFoundKeyPoint() {
    return foundKeyPoint;
  }
  public int getFoundShapeIndex() {
    return vec.indexOf(foundShape);
  }

  /**
   * Shape Objekt löschen, welches beim letzten Aufruf von isShapePointAt() gefunden wurde
   */
  public void removeFoundShape() {
    if (foundShape!=null)
      vec.remove(foundShape);
  }

  /**
   * ruft für jedes Shape-Objekt tryAddPoint() Methode auf
   */
  public void addPoint(int x, int y)
  {
    for (int i=0; i<size(); i++) {
      Shape s=(Shape)vec.get(i);
      if (s.tryAddPoint(x,y)) return;
    }
  }

  public void removePointAt(int x, int y) {
    for (int i=0; i<size(); i++) {
      Shape s=(Shape)vec.get(i);
      if (s.removePointAt(x,y)) return;
    }
  }

  public String get_html(String imgSrc, int w, int h) {
    String str = new String("<img src=\"" + imgSrc + "\" width=\"" + w + "\" height=\"" + h + "\" usemap=\"#"+mapName+"\" border=\"0\">\n<map name=\""+mapName+"\">\n");
    for (int i=0; i<size(); i++) {
      Shape s=(Shape)vec.get(i);
      str+=s.get_html();
    }
    str+="</map>";
    return str;
  }
}