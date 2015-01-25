package com.quollwriter.ui;

import java.awt.Color;
import java.awt.Component;
import java.awt.Container;
import java.awt.Cursor;
import java.awt.Desktop;
import java.awt.Dialog;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Frame;
import java.awt.GraphicsEnvironment;
import java.awt.Insets;
import java.awt.KeyboardFocusManager;
import java.awt.Toolkit;
import java.awt.Window;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.Graphics;
import java.awt.Point;
import java.awt.Image;
import java.awt.image.BufferedImage;
import java.awt.Rectangle;
import java.awt.datatransfer.*;
import java.awt.event.*;
import java.awt.font.*;
import java.awt.image.*;

import java.net.*;

import java.io.*;

import java.text.*;

import java.util.*;
import java.util.regex.*;

import javax.swing.*;
import javax.swing.border.*;
import javax.swing.event.*;
import javax.swing.text.*;
import javax.swing.text.html.*;
import javax.swing.tree.*;

import javax.imageio.*;
import javax.activation.*;

import com.gentlyweb.utils.StringUtils;

import org.imgscalr.Scalr;

import com.jgoodies.forms.builder.*;
import com.jgoodies.forms.factories.*;
import com.jgoodies.forms.layout.*;

import com.quollwriter.*;

import com.quollwriter.data.*;
import com.quollwriter.data.comparators.*;

import com.quollwriter.events.*;

import com.quollwriter.ui.actionHandlers.*;
import com.quollwriter.ui.components.*;
import com.quollwriter.ui.renderers.*;
import com.quollwriter.ui.events.*;
import com.quollwriter.ui.panels.*;
import com.quollwriter.ui.sidebars.*;

import com.quollwriter.text.*;

import org.jfree.chart.*;
import org.jfree.chart.axis.*;
import org.jfree.chart.labels.*;
import org.jfree.chart.plot.*;
import org.jfree.chart.renderer.xy.*;

import org.jfree.data.time.*;

import org.jfree.ui.*;

import org.josql.utils.*;


public class UIUtils
{

    public static int DEFAULT_POPUP_WIDTH = 450;

    public static Font headerFont = null; 

    public static final Border textFieldSpacing = new EmptyBorder (3,
                                                                   3,
                                                                   3,
                                                                   3);

    private static final QTextEditor pageCountTextEditor = new QTextEditor (null,
                                                                            false,
                                                                            QuollEditorPanel.SECTION_BREAK);                         
                   
    public static int getPopupWidth ()
    {
        
        return DEFAULT_POPUP_WIDTH;
        
                                                                   
    }
    public static TreePath getTreePathForUserObject (DefaultMutableTreeNode node,
                                                     Object                 o)
    {

        Object nObj  = node.getUserObject ();
        
        if (nObj instanceof SelectableDataObject)
        {

            nObj = ((SelectableDataObject) nObj).obj;

        }

        if (nObj instanceof DataObject)
        {
        
            if (nObj.equals (o))
            {
    
                return new TreePath (node.getPath ());
    
            }

        }

        if ((nObj instanceof Segment)
            &&
            (o instanceof Segment)
           )
        {
        
            if (nObj.toString ().equals (o.toString ()))
            {
    
                return new TreePath (node.getPath ());
    
            }

        }
        
        if (nObj == o)
        {
            
            return new TreePath (node.getPath ());
            
        }
        
        Enumeration en = node.children ();

        while (en.hasMoreElements ())
        {

            node = (DefaultMutableTreeNode) en.nextElement ();

            TreePath tp = UIUtils.getTreePathForUserObject (node,
                                                            o);

            if (tp != null)
            {

                return tp;

            }

        }

        return null;

    }

    public static void createTree (Collection             items,
                                   DefaultMutableTreeNode parent)
    {

        Iterator iter = items.iterator ();

        while (iter.hasNext ())
        {

            Object v = iter.next ();

            if (v instanceof Map)
            {

                UIUtils.createTree ((Map) v,
                                    parent);

                continue;

            }

            if (v instanceof Collection)
            {

                UIUtils.createTree ((Collection) v,
                                    parent);

                continue;

            }

            DefaultMutableTreeNode n = new DefaultMutableTreeNode (v);

            parent.add (n);

        }

    }

    public static void createTree (Map                    items,
                                   DefaultMutableTreeNode parent)
    {

        Iterator iter = items.entrySet ().iterator ();

        while (iter.hasNext ())
        {

            Map.Entry item = (Map.Entry) iter.next ();

            Object k = item.getKey ();

            Object v = item.getValue ();

            // Add a node for the key.
            DefaultMutableTreeNode key = new DefaultMutableTreeNode (k);

            parent.add (key);

            if (v instanceof Map)
            {

                UIUtils.createTree ((Map) v,
                                    key);

            }

            if (v instanceof Collection)
            {

                UIUtils.createTree ((Collection) v,
                                    key);

            }

        }

    }

    public static DefaultMutableTreeNode createAssetTree (String  objType,
                                                          Project p)
    {

        List<NamedObject> objs = new ArrayList (p.getAllNamedChildObjects (Asset.getAssetClass (objType)));
        
        DefaultMutableTreeNode root = new DefaultMutableTreeNode (p);

        Collections.sort (objs,
                          new NamedObjectSorter ());

        for (NamedObject obj : objs)
        {                          
        
            root.add (new DefaultMutableTreeNode (obj));

        }

        return root;
        
    }
/*    
    public static DefaultMutableTreeNode createLocationsTree (Project p)
    {

        DefaultMutableTreeNode root = new DefaultMutableTreeNode (p);

        Collections.sort (p.getLocations (),
                          new NamedObjectSorter ());

        for (Location ll : p.getLocations ())
        {

            root.add (new DefaultMutableTreeNode (ll));

        }

        return root;
        
    }
  */                                             
    public static DefaultMutableTreeNode createAssetsTree (Project p)
    {

        DefaultMutableTreeNode root = new DefaultMutableTreeNode (p);

        TreeParentNode c = new TreeParentNode (QCharacter.OBJECT_TYPE,
                                               Environment.getObjectTypeNamePlural (QCharacter.OBJECT_TYPE));

        DefaultMutableTreeNode tn = new DefaultMutableTreeNode (c);

        root.add (tn);

        Collections.sort (p.getCharacters (),
                          new NamedObjectSorter ());

        for (int i = 0; i < p.getCharacters ().size (); i++)
        {

            tn.add (new DefaultMutableTreeNode (p.getCharacters ().get (i)));

        }

        TreeParentNode l = new TreeParentNode (Location.OBJECT_TYPE,
                                               Environment.getObjectTypeNamePlural (Location.OBJECT_TYPE));

        tn = new DefaultMutableTreeNode (l);

        Collections.sort (p.getLocations (),
                          new NamedObjectSorter ());

        for (Location ll : p.getLocations ())
        {

            tn.add (new DefaultMutableTreeNode (ll));

        }

        root.add (tn);

        TreeParentNode o = new TreeParentNode (QObject.OBJECT_TYPE,
                                               Environment.getObjectTypeNamePlural (QObject.OBJECT_TYPE));

        tn = new DefaultMutableTreeNode (o);

        Collections.sort (p.getQObjects (),
                          new NamedObjectSorter ());

        for (QObject oo : p.getQObjects ())
        {

            tn.add (new DefaultMutableTreeNode (oo));

        }

        root.add (tn);

        TreeParentNode r = new TreeParentNode (ResearchItem.OBJECT_TYPE,
                                               Environment.getObjectTypeNamePlural (ResearchItem.OBJECT_TYPE));

        tn = new DefaultMutableTreeNode (r);

        Collections.sort (p.getResearchItems (),
                          new NamedObjectSorter ());

        for (ResearchItem rr : p.getResearchItems ())
        {

            tn.add (new DefaultMutableTreeNode (rr));

        }

        root.add (tn);

        return root;

    }

    public static void getSelectedObjects (DefaultMutableTreeNode n,
                                           NamedObject            addTo,
                                           Set                    s)
                                    throws GeneralException
    {

        Enumeration<DefaultMutableTreeNode> en = n.children ();

        while (en.hasMoreElements ())
        {

            DefaultMutableTreeNode nn = en.nextElement ();

            SelectableDataObject sd = (SelectableDataObject) nn.getUserObject ();

            if (sd.selected)
            {

                s.add (new Link (addTo,
                                 sd.obj));

            }

            UIUtils.getSelectedObjects (nn,
                                        addTo,
                                        s);

        }

    }

    public static JTree createLinkedToTree (final AbstractProjectViewer projectViewer,
                                            final NamedObject           dataObject,
                                            boolean                     editMode)
    {

        final JTree tree = UIUtils.createTree ();

        if (editMode)
        {

            tree.setCellRenderer (new SelectableProjectTreeCellRenderer ());

        } else
        {

            tree.setCellRenderer (new ProjectTreeCellRenderer (true));

        }

        tree.setOpaque (true);
        tree.setBorder (null);

        tree.setRootVisible (false);
        tree.setShowsRootHandles (true);
        tree.setScrollsOnExpand (true);

        if (editMode)
        {

            // Never toggle.
            // tree.setToggleClickCount (-1);

            tree.addMouseListener (new MouseAdapter ()
                {

                    private void selectAllChildren (DefaultTreeModel       model,
                                                    DefaultMutableTreeNode n,
                                                    boolean                v)
                    {

                        Enumeration<DefaultMutableTreeNode> en = n.children ();

                        while (en.hasMoreElements ())
                        {

                            DefaultMutableTreeNode c = en.nextElement ();

                            SelectableDataObject s = (SelectableDataObject) c.getUserObject ();

                            s.selected = v;

                            // Tell the model that something has changed.
                            model.nodeChanged (c);

                            // Iterate.
                            this.selectAllChildren (model,
                                                    c,
                                                    v);

                        }

                    }

                    public void mousePressed (MouseEvent ev)
                    {

                        TreePath tp = tree.getPathForLocation (ev.getX (),
                                                               ev.getY ());

                        if (tp != null)
                        {

                            DefaultMutableTreeNode n = (DefaultMutableTreeNode) tp.getLastPathComponent ();

                            // Tell the model that something has changed.
                            DefaultTreeModel model = (DefaultTreeModel) tree.getModel ();

                            SelectableDataObject s = (SelectableDataObject) n.getUserObject ();

                            /*
                                                if ((ev.getClickCount () == 2)
                                                    &&
                                                    (n.getChildCount () > 0)
                                                   )
                                                {

                                                    this.selectAllChildren (model,
                                                                            n,
                                                                            s.selected);

                                                } else {

                                                    s.selected = !s.selected;

                                                }
                             */
                            s.selected = !s.selected;

                            model.nodeChanged (n);

                        }

                    }

                });

        } else
        {

            tree.addMouseListener (new MouseAdapter ()
                {

                    public void mousePressed (MouseEvent ev)
                    {

                        TreePath tp = tree.getPathForLocation (ev.getX (),
                                                               ev.getY ());

                        if (tp != null)
                        {

                            DefaultMutableTreeNode n = (DefaultMutableTreeNode) tp.getLastPathComponent ();

                            if ((n.getChildCount () == 0) &&
                                (ev.getClickCount () == 2))
                            {

                                projectViewer.viewObject ((DataObject) n.getUserObject ());

                            }

                        }

                    }

                });

        }

        tree.putClientProperty (com.jgoodies.looks.Options.TREE_LINE_STYLE_KEY,
                                com.jgoodies.looks.Options.TREE_LINE_STYLE_NONE_VALUE);

        tree.putClientProperty ("Tree.paintLines",
                                Boolean.FALSE);

        tree.setModel (UIUtils.getLinkedToTreeModel (projectViewer,
                                                     dataObject,
                                                     editMode));

        UIUtils.expandPathsForLinkedOtherObjects (tree,
                                                  dataObject);

        return tree;

    }

    public static void scrollIntoView (final JComponent c)
    {
        
        Environment.scrollIntoView (c);
/*                
        JComponent p = (JComponent) c.getParent ();
        
        JComponent pp = c;
        
        while (p != null)
        {
            
            if (p instanceof JScrollPane)
            {
            
                final JScrollPane sp = (JScrollPane) p;
                
                SwingUtilities.invokeLater (new Runnable ()
                {
                    
                    public void run ()
                    {
                    
                        sp.getVerticalScrollBar ().setValue (c.getBounds (null).y);
                        
                    }
                    
                });

                return;
                
            }
            
            pp = p;
            
            p = (JComponent) p.getParent ();
            
        }
  */                      
    }
    
    public static void expandPathsForLinkedOtherObjects (JTree       tree,
                                                         NamedObject d)
    {
        
        DefaultMutableTreeNode root = (DefaultMutableTreeNode) ((DefaultTreeModel) tree.getModel ()).getRoot ();

        for (Link l : d.getLinks ())
        {

            // Get the path for the object.
            TreePath tp = UIUtils.getTreePathForUserObject (root,
                                                            l.getOtherObject (d));

            if (tp != null)
            {

                tree.expandPath (tp.getParentPath ());
                
            }

        }        
        
    }

    public static DefaultTreeModel getLinkedToTreeModel (AbstractProjectViewer projectViewer,
                                                         NamedObject           dataObject,
                                                         boolean               editMode)
    {

        List exclude = new ArrayList ();
        exclude.add (dataObject);

        // Painful but just about the only way.
        projectViewer.setLinks (dataObject);

        // Get all the "other objects" for the links the note has.
        Iterator<Link> it = dataObject.getLinks ().iterator ();

        Set links = new HashSet ();

        while (it.hasNext ())
        {

            links.add (it.next ().getOtherObject (dataObject));

        }
        
        return new DefaultTreeModel (UIUtils.createLinkToTree (projectViewer.getProject (),
                                                               exclude,
                                                               links,
                                                               editMode));

    }

    public static Box createLinkedToItemsBox (final NamedObject           obj,
                                              final AbstractProjectViewer pv,
                                              final QPopup                p,
                                              boolean                     addTitle)
    {

        Box pa = new Box (BoxLayout.Y_AXIS);
        pa.setOpaque (false);

        if (addTitle)
        {

            JLabel h = new JLabel ("Linked to");
            pa.add (h);
            pa.add (Box.createVerticalStrut (3));

        }

        Set<NamedObject> sl = obj.getOtherObjectsInLinks ();

        Iterator<NamedObject> liter = sl.iterator ();

        while (liter.hasNext ())
        {

            final NamedObject other = liter.next ();

            JLabel l = new JLabel (other.getName ());
            l.setOpaque (false);
            l.setIcon (Environment.getIcon (other.getObjectType (),
                                            Constants.ICON_MENU));
            l.setToolTipText ("Click to view the item");
            l.setCursor (Cursor.getPredefinedCursor (Cursor.HAND_CURSOR));
            l.setForeground (Color.BLUE);
            l.setBorder (new EmptyBorder (0,
                                          5,
                                          0,
                                          0));

            l.setAlignmentX (Component.LEFT_ALIGNMENT);

            pa.add (l);
            pa.add (Box.createVerticalStrut (3));

            l.addMouseListener (new MouseAdapter ()
                {

                    public void mouseReleased (MouseEvent ev)
                    {

                        // Prevents the popup from disappearing.
                        pv.viewObject (other);

                        if (p != null)
                        {

                            // Close the popup.
                            p.setVisible (false);

                        }

                    }

                });

        }

        pa.setPreferredSize (new Dimension (380,
                                            pa.getPreferredSize ().height));

        return pa;

    }

    public static QPopup createClosablePopup (final String         title,
                                              final Icon           icon,
                                              final ActionListener onClose)
    {
        
        final QPopup qp = new QPopup (Environment.replaceObjectNames (title),
                                      icon,
                                      null)
        {
            
            public void setVisible (boolean v)
            {

                if ((!v)
                    &&
                    (onClose != null)
                   )
                {

                    onClose.actionPerformed (new ActionEvent (this,
                                                              0,
                                                              "closing"));

                }

                super.setVisible (v);

            }

        };

        JButton close = UIUtils.createButton (Constants.CLOSE_ICON_NAME,
                                              Constants.ICON_MENU,
                                              "Click to close",
                                              new ActionAdapter ()
        {

            public void actionPerformed (ActionEvent ev)
            {
        
                qp.setVisible (false);
                
                if (onClose != null)
                {
                    
                    onClose.actionPerformed (ev);
                    
                }

                qp.removeFromParent ();
                
            }
            
        });
        
        List<JButton> buts = new ArrayList ();
        buts.add (close);
        
        qp.getHeader ().setControls (UIUtils.createButtonBar (buts));
                                                    
        return qp;        
        
    }
    
    public static DefaultMutableTreeNode createChapterNotesNode (Chapter c)
    {

        if (!Environment.getUserProperties ().getPropertyAsBoolean (Constants.SHOW_NOTES_IN_CHAPTER_LIST_PROPERTY_NAME))
        {
            
            return null;
            
        }
    
        Set<Note> notes = c.getNotes ();
        
        if ((notes == null)
            ||
            (notes.size () == 0)
           )
        {
            
            return null;
            
        }
    
        TreeParentNode nullN = new TreeParentNode (Note.OBJECT_TYPE,
                                                   Environment.replaceObjectNames ("{Notes}"),
                                                   notes.size ());

        DefaultMutableTreeNode root = new DefaultMutableTreeNode (nullN);

        for (Note n : c.getNotes ())
        {
        
            DefaultMutableTreeNode node = UIUtils.createTreeNode (n,
                                                                  null,
                                                                  null,
                                                                  false);

            if (node == null)
            {

                continue;

            }

            root.add (node);

        }
        
        if ((c.getNotes () == null)
            ||
            (c.getNotes ().size () == 0)
           )
        {
            
            return null;
            
        }
        
        return root;

    }
    
    public static DefaultMutableTreeNode createNoteTree (AbstractProjectViewer pv)
    {

        DefaultMutableTreeNode root = new DefaultMutableTreeNode (pv.getProject ());

        TypesHandler noteTypeHandler = pv.getObjectTypesHandler (Note.OBJECT_TYPE);//getNoteTypeHandler ();
        
        //Set<Note> notes = pv.getAllNotes ();

        //Set<String> types = noteTypeHandler.getTypesFromObjects ();

        Map<String, Set<Note>> typeNotes = noteTypeHandler.getObjectsAgainstTypes ();
        
        Map<String, DefaultMutableTreeNode> noteNodes = new HashMap ();

        TreeParentNode nullN = new TreeParentNode (Note.OBJECT_TYPE,
                                                   "No Type");

        DefaultMutableTreeNode nullNode = new DefaultMutableTreeNode (nullN);

        noteNodes.put (null,
                       nullNode);

        Set<String> types = typeNotes.keySet ();
                       
        for (String type : types)
        {

            boolean added = false;

            Set<Note> notes = typeNotes.get (type);
            
            for (Note n : notes)
            {

                String t = n.getType ();

                DefaultMutableTreeNode noteNode = null;

                if ((t == null) ||
                    (t.equals ("")))
                {

                    noteNode = noteNodes.get (null);

                } else
                {

                    if (t.equals (type))
                    {

                        if (!added)
                        {

                            added = true;

                            TreeParentNode b = new TreeParentNode (Note.OBJECT_TYPE,
                                                                   type,
                                                                   notes.size ());
                                                                   //noteTypeHandler.getObjectsForType (t).size ());
                                                                   
                            DefaultMutableTreeNode tn = new DefaultMutableTreeNode (b);

                            root.add (tn);

                            noteNodes.put (type,
                                           tn);

                        }

                        noteNode = noteNodes.get (t);

                    }

                }

                if (noteNode != null)
                {

                    DefaultMutableTreeNode nn = new DefaultMutableTreeNode (n);

                    noteNode.add (nn);

                }

            }

        }

        if (nullNode.getChildCount () > 0)
        {

            // Insert at the top.
            root.insert (nullNode,
                         0);

        }
/*
        DefaultMutableTreeNode root = new DefaultMutableTreeNode (p);

        Iterator<Note> iter = notes.keySet ().iterator ();

        while (iter.hasNext ())
        {

            String t = iter.next ();

            List<Note> l = allNotes.get (t);

            // Peek at the top of the list to see the actual type.
            t = l.get (0).getType ();

            Note b = new Note (0,
                               null);
            b.setName (t);

            DefaultMutableTreeNode tn = new DefaultMutableTreeNode (b);

            root.add (tn);

            for (int i = 0; i < l.size (); i++)
            {

                DefaultMutableTreeNode n = new DefaultMutableTreeNode (l.get (i));

                tn.add (n);

            }

        }
*/
        return root;

    }

    public static JCheckBox createCheckBox (String text)
    {
        
        JCheckBox b = new JCheckBox (Environment.replaceObjectNames (text));
        
        b.setBackground (null);
        b.setOpaque (false);
        
        return b;
        
    }

    public static DefaultMutableTreeNode createChaptersTree (Project    p,
                                                             Collection exclude,
                                                             Collection init,
                                                             boolean    selectable)
    {

        DefaultMutableTreeNode root = UIUtils.createTreeNode (p,
                                                              exclude,
                                                              init,
                                                              selectable);

        if (p.getBooks ().size () == 1)
        {

            Book b = (Book) p.getBooks ().get (0);

            // Get the chapters.
            List<Chapter> chaps = b.getChapters ();

            for (Chapter c : chaps)
            {

                DefaultMutableTreeNode node = UIUtils.createTree (c,
                                                                  exclude,
                                                                  init,
                                                                  selectable);

                if (node == null)
                {

                    continue;

                }

                root.add (node);

            }

        } else
        {

            List<Book> books = p.getBooks ();

            for (int i = 0; i < books.size (); i++)
            {

                Book b = books.get (i);

                DefaultMutableTreeNode node = UIUtils.createTree (b,
                                                                  exclude,
                                                                  init,
                                                                  selectable);

                if (node == null)
                {

                    continue;

                }

                root.add (node);

            }

        }

        return root;

    }

    public static void addAssetsToLinkToTree (DefaultMutableTreeNode root,
                                              List<? extends Asset>  assets,
                                              Collection             exclude,
                                              Collection             init,
                                              boolean                selectable)
    {

        if (assets == null)
        {

            return;

        }

        DefaultMutableTreeNode charn = null;

        if (assets.size () > 0)
        {

            if (selectable)
            {
        
                // Peek at the top to find out the type.
                BlankNamedObject bdo = new BlankNamedObject (assets.get (0).getObjectType (),
                                                             Environment.getObjectTypeNamePlural (assets.get (0)));
    
                Object o = bdo;

                SelectableDataObject s = new SelectableDataObject (bdo);
                s.parentNode = true;

                o = s;

                charn = new DefaultMutableTreeNode (o);
                
                root.add (charn);
                
                root = charn;
                
            }

            for (Asset a : assets)
            {

                if (!selectable)
                {

                    if (!init.contains (a))
                    {

                        continue;

                    }

                }

                DefaultMutableTreeNode node = UIUtils.createTreeNode (a,
                                                                      exclude,
                                                                      init,
                                                                      selectable);

                if (node == null)
                {

                    continue;

                }

                root.add (node);
                
            }
/*
            if (charn.getChildCount () > 0)
            {

                root.add (charn);

            }
*/
        }

    }

    public static DefaultMutableTreeNode createLinkToTree (Project    p,
                                                           Collection exclude,
                                                           Collection init,
                                                           boolean    selectable)
    {

        DefaultMutableTreeNode root = UIUtils.createTreeNode (p,
                                                              exclude,
                                                              init,
                                                              selectable);

        if (p.getBooks ().size () == 1)
        {

            Book b = (Book) p.getBooks ().get (0);

            // Get the chapters.
            List<Chapter> chaps = b.getChapters ();

            for (Chapter c : chaps)
            {

                if (!selectable)
                {

                    if (!init.contains (c))
                    {

                        continue;

                    }

                }

                DefaultMutableTreeNode node = UIUtils.createTree (c,
                                                                  exclude,
                                                                  init,
                                                                  selectable);

                if (node == null)
                {

                    continue;

                }

                root.add (node);

            }

        } else
        {

            List<Book> books = p.getBooks ();

            for (int i = 0; i < books.size (); i++)
            {

                Book b = books.get (i);

                if (!selectable)
                {

                    if (!init.contains (b))
                    {

                        continue;

                    }

                }

                DefaultMutableTreeNode node = UIUtils.createTree (b,
                                                                  exclude,
                                                                  init,
                                                                  selectable);

                if (node == null)
                {

                    continue;

                }

                root.add (node);

            }

        }

        // Sort the characters.

        // Get all the characters.
        UIUtils.addAssetsToLinkToTree (root,
                                       p.getCharacters (),
                                       exclude,
                                       init,
                                       selectable);

        // Get all the locations.
        UIUtils.addAssetsToLinkToTree (root,
                                       p.getLocations (),
                                       exclude,
                                       init,
                                       selectable);

        UIUtils.addAssetsToLinkToTree (root,
                                       p.getQObjects (),
                                       exclude,
                                       init,
                                       selectable);

        UIUtils.addAssetsToLinkToTree (root,
                                       p.getResearchItems (),
                                       exclude,
                                       init,
                                       selectable);

        Map<String, Set<Note>> allNotes = new TreeMap ();

        List<Book> books = p.getBooks ();

        // Collect all the notes.
        for (int i = 0; i < books.size (); i++)
        {

            List<Chapter> chapters = books.get (i).getChapters ();

            for (int j = 0; j < chapters.size (); j++)
            {

                Set<Note> notes = chapters.get (j).getNotes ();

                for (Note n : notes)
                {

                    String t = n.getType ();

                    Set<Note> l = allNotes.get (t);

                    if (l == null)
                    {

                        l = new TreeSet (new ChapterItemSorter ());

                        allNotes.put (t,
                                      l);

                    }

                    l.add (n);

                }

            }

        }

        Iterator<Map.Entry<String, Set<Note>>> iter = allNotes.entrySet ().iterator ();

        while (iter.hasNext ())
        {

            Map.Entry<String, Set<Note>> item = iter.next ();

            String t = item.getKey ();

            Set<Note> l = item.getValue ();

            // Peek at the top of the list to see the actual type.
            t = l.iterator ().next ().getType ();
            //t = l.get (0).getType ();

            BlankNamedObject bdo = new BlankNamedObject (Note.OBJECT_TYPE,
                                                         t);

            Object o = bdo;

            if (selectable)
            {

                SelectableDataObject s = new SelectableDataObject (bdo);
                s.parentNode = true;
                o = s;

            }

            DefaultMutableTreeNode tn = new DefaultMutableTreeNode (o);

            for (Note n : l)
            {

                if (!selectable)
                {

                    if (!init.contains (n))
                    {

                        continue;

                    }

                }

                DefaultMutableTreeNode node = UIUtils.createTreeNode (n,
                                                                      exclude,
                                                                      init,
                                                                      selectable);

                if (node != null)
                {

                    tn.add (node);

                }

            }

            if (tn.getChildCount () > 0)
            {

                root.add (tn);

            }

        }

        return root;

    }

    public static void expandAllNodesWithChildren (JTree t)
    {

        DefaultTreeModel dtm = (DefaultTreeModel) t.getModel ();

        DefaultMutableTreeNode root = (DefaultMutableTreeNode) dtm.getRoot ();

        Enumeration en = root.depthFirstEnumeration ();

        while (en.hasMoreElements ())
        {

            DefaultMutableTreeNode node = (DefaultMutableTreeNode) en.nextElement ();

            if (node.getChildCount () > 0)
            {

                t.expandPath (new TreePath (node.getPath ()));

            }

        }

    }

    public static void getSelectedObjects (DefaultMutableTreeNode n,
                                           Set                    s)
    {

        Enumeration<DefaultMutableTreeNode> en = n.children ();

        while (en.hasMoreElements ())
        {

            DefaultMutableTreeNode nn = en.nextElement ();

            SelectableDataObject sd = (SelectableDataObject) nn.getUserObject ();

            if (sd.selected)
            {

                s.add (sd.obj);

            }

            UIUtils.getSelectedObjects (nn,
                                        s);

        }

    }

    public static void addSelectableListener (final JTree tree)
    {

        tree.addMouseListener (new MouseAdapter ()
            {

                private void selectAllChildren (DefaultTreeModel       model,
                                                DefaultMutableTreeNode n,
                                                boolean                v)
                {

                    Enumeration<DefaultMutableTreeNode> en = n.children ();

                    while (en.hasMoreElements ())
                    {

                        DefaultMutableTreeNode c = en.nextElement ();

                        SelectableDataObject s = (SelectableDataObject) c.getUserObject ();

                        s.selected = v;

                        // Tell the model that something has changed.
                        model.nodeChanged (c);

                        // Iterate.
                        this.selectAllChildren (model,
                                                c,
                                                v);

                    }

                }

                public void mouseClicked (MouseEvent ev)
                {

                    TreePath tp = tree.getPathForLocation (ev.getX (),
                                                           ev.getY ());

                    if (tp != null)
                    {

                        DefaultMutableTreeNode n = (DefaultMutableTreeNode) tp.getLastPathComponent ();

                        // Tell the model that something has changed.
                        DefaultTreeModel model = (DefaultTreeModel) tree.getModel ();

                        SelectableDataObject s = (SelectableDataObject) n.getUserObject ();
/*
                    if ((ev.getClickCount () == 2)
                        &&
                        (n.getChildCount () > 0)
                       )
                    {

                        this.selectAllChildren (model,
                                                n,
                                                s.selected);

                    } else {

                        s.selected = !s.selected;

                    }
*/
                        s.selected = !s.selected;

                        model.nodeChanged (n);

                    }

                }

            });

    }

    public static DefaultMutableTreeNode createTree (Book       b,
                                                     Collection exclude,
                                                     Collection init,
                                                     boolean    selectable)
    {

        DefaultMutableTreeNode root = UIUtils.createTreeNode (b,
                                                              exclude,
                                                              init,
                                                              selectable);

        if (root == null)
        {

            return null;

        }

        // Get the chapters.
        List<Chapter> chs = b.getChapters ();

        for (int i = 0; i < chs.size (); i++)
        {

            Chapter c = chs.get (i);

            if (!selectable)
            {

                if ((init != null)
                    &&
                    (!init.contains (c))
                   )
                {

                    continue;

                }

            }

            DefaultMutableTreeNode node = UIUtils.createTree (c,
                                                              exclude,
                                                              init,
                                                              selectable);

            if (node == null)
            {

                continue;

            }

            root.add (node);

        }

        return root;

    }

    private static DefaultMutableTreeNode createTreeNode (Object     o,
                                                          Collection exclude,
                                                          Collection init,
                                                          boolean    selectable)
    {

        if ((exclude != null) &&
            (exclude.contains (o)))
        {

            return null;

        }

        if (selectable)
        {

            SelectableDataObject so = new SelectableDataObject ((NamedObject) o);

            if ((init != null)
                &&
                (init.contains (o))
               )
            {

                so.selected = true;

            }

            o = so;

        }

        return new DefaultMutableTreeNode (o);

    }

    public static DefaultMutableTreeNode createTree (Scene      s,
                                                     Collection exclude,
                                                     Collection init,
                                                     boolean    selectable)
    {

        DefaultMutableTreeNode root = UIUtils.createTreeNode (s,
                                                              exclude,
                                                              init,
                                                              selectable);

        if (root == null)
        {

            return null;

        }

        // Get the outline items.
        Iterator iter = s.getOutlineItems ().iterator ();

        while (iter.hasNext ())
        {

            Object o = iter.next ();

            if (!selectable)
            {

                if ((init != null) &&
                    (!init.contains (o)))
                {

                    continue;

                }

            }

            DefaultMutableTreeNode node = UIUtils.createTreeNode (o,
                                                                  exclude,
                                                                  init,
                                                                  selectable);

            if (node == null)
            {

                continue;

            }

            root.add (node);

        }

        return root;

    }

    public static DefaultMutableTreeNode createTree (Chapter    c,
                                                     Collection exclude,
                                                     Collection init,
                                                     boolean    selectable)
    {

        DefaultMutableTreeNode root = UIUtils.createTreeNode (c,
                                                              exclude,
                                                              init,
                                                              selectable);

        if (root == null)
        {

            return null;

        }

        List items = new ArrayList (c.getScenes ());

        items.addAll (c.getOutlineItems ());
      
        Collections.sort (items,
                          new ChapterItemSorter ());

        for (int j = 0; j < items.size (); j++)
        {

            ChapterItem i = (ChapterItem) items.get (j);
/*
            if (i instanceof Note)
            {

                Note n = (Note) i;

                DefaultMutableTreeNode node = UIUtils.createTreeNode ((Note) n,
                                                                      exclude,
                                                                      init,
                                                                      false);

                if (node == null)
                {

                    continue;

                }

                root.add (node);

            }
*/
            if (i instanceof Scene)
            {

                Scene s = (Scene) i;

                if (!selectable)
                {

                    if ((init != null) &&
                        (!init.contains (s)))
                    {

                        continue;

                    }

                }

                DefaultMutableTreeNode node = UIUtils.createTree ((Scene) s,
                                                                  exclude,
                                                                  init,
                                                                  selectable);

                if (node == null)
                {

                    continue;

                }

                root.add (node);

            }

            if (i instanceof OutlineItem)
            {

                OutlineItem oi = (OutlineItem) i;

                if (!selectable)
                {

                    if ((init != null) &&
                        (!init.contains (oi)))
                    {

                        continue;

                    }

                }

                DefaultMutableTreeNode node = UIUtils.createTreeNode (oi,
                                                                      exclude,
                                                                      init,
                                                                      selectable);

                if (node == null)
                {

                    continue;

                }

                root.add (node);

            }

        }

        if (!selectable)
        {
        
            DefaultMutableTreeNode notesNode = UIUtils.createChapterNotesNode (c);
    
            if (notesNode != null)
            {
                
                root.add (notesNode);
                
            }

        }
        
        return root;

    }

    private static void showErrorMessage (PopupsSupported parent,
                                         String    message)
    {

        final Box content = new Box (BoxLayout.Y_AXIS);

        AbstractProjectViewer pv = null;
        
        if (parent instanceof AbstractProjectViewer)
        {
            
            pv = (AbstractProjectViewer) parent;
            
        }
        
        JTextPane m = UIUtils.createHelpTextPane (message + "<br /><br /><a href='qw:/report-a-bug'>Click here to contact Quoll Writer support about this problem.</a>",
                                                  pv);
        m.setSize (new Dimension (DEFAULT_POPUP_WIDTH - 20,
                                  m.getPreferredSize ().height));
        m.setBorder (null);
        content.add (m);

        content.add (Box.createVerticalStrut (10));
        
        JButton close = UIUtils.createButton ("Close",
                                              null);
        
        JButton[] buts = new JButton[] { close };
        
        JComponent buttons = UIUtils.createButtonBar2 (buts,
                                                       Component.LEFT_ALIGNMENT); 
        buttons.setAlignmentX (Component.LEFT_ALIGNMENT);
        content.add (buttons);
        content.setBorder (new EmptyBorder (10, 10, 10, 10));
        
        final QPopup ep = UIUtils.createPopup ("Oops, an error has occurred...",
                                               Constants.ERROR_ICON_NAME,
                                               content,
                                               true,
                                               null);

        close.addActionListener (new ActionListener ()
        {
            
            public void actionPerformed (ActionEvent ev)
            {
                
                ep.removeFromParent ();                
                                         
            }
        });
        
        content.setPreferredSize (new Dimension (DEFAULT_POPUP_WIDTH,
                                                 content.getPreferredSize ().height));
                                            
        parent.showPopupAt (ep,
                            UIUtils.getCenterShowPosition ((Component) parent,
                                                           ep));
        ep.setDraggable ((Component) parent);

    }

    /**
     * And this convoluted mess is what happens when you change something fundemantal half way
     * through!  This needs to be cleaned up in a future release.
     *
     * @param parent The parent to show the error against.
     * @param message The message to show.
     */
    public static void showErrorMessage (Component parent,
                                         String    message)
    {
        
        if (parent == null)
        {
            
            UIUtils.showErrorMessage (message);
            
            return;
            
        }
        
        if (parent instanceof PopupsSupported)
        {
            
            UIUtils.showErrorMessage ((PopupsSupported) parent,
                                      message);
            
            return;
            
        }
        
        if (parent instanceof PopupWindow)
        {
            
            UIUtils.showErrorMessage ((PopupWindow) parent,
                                      message);
            
            return;
            
        }

        if (parent instanceof PopupWindow)
        {
            
            UIUtils.showErrorMessage (((PopupWindow) parent).getProjectViewer (),
                                      message);
            
            return;
            
        }
        
        if (parent instanceof QuollPanel)
        {
            
            UIUtils.showErrorMessage ((PopupsSupported) ((QuollPanel) parent).getProjectViewer (),
                                      message);
            
            return;
            
        }
    
        UIUtils.showErrorMessage (message);
        
    }

    private static void showErrorMessage (AbstractProjectViewer pv,
                                          String                message)
    {
        
        ErrorWindow ew = new ErrorWindow (pv,
                                          message);
        
        ew.init ();

    }
    
    private static void showErrorMessage (PopupWindow p,
                                          String      message)
    {
        
        ErrorWindow ew = new ErrorWindow (p.getProjectViewer (),
                                          message);
        
        ew.init ();

        Rectangle pbounds = p.getBounds ();

        Dimension size = ew.getPreferredSize ();
        
        int x = ((pbounds.width - size.width) / 2) + pbounds.x;
        int y = ((pbounds.height - size.height) / 2) + pbounds.y;

        // Move the window
        Point showAt = new Point (x,
                                  y);
        
        ew.setShowAt (showAt);
        
    }

    private static void showErrorMessage (String      message)
    {

        ErrorWindow ew = new ErrorWindow (null,
                                          message);
        
        ew.init ();

    }

    private static void showMessage (PopupsSupported parent,
                                     String          title,
                                     String          message)
    {

        final Box content = new Box (BoxLayout.Y_AXIS);

        AbstractProjectViewer pv = null;
        
        if (parent instanceof AbstractProjectViewer)
        {
            
            pv = (AbstractProjectViewer) parent;
            
        }
        
        JTextPane m = UIUtils.createHelpTextPane (message,
                                                  pv);
        m.setSize (new Dimension (DEFAULT_POPUP_WIDTH - 20,
                                  m.getPreferredSize ().height));
        m.setBorder (null);
        content.add (m);

        content.add (Box.createVerticalStrut (10));
        
        JButton close = UIUtils.createButton ("Close",
                                              null);
        
        JButton[] buts = new JButton[] { close };
        
        JComponent buttons = UIUtils.createButtonBar2 (buts,
                                                       Component.LEFT_ALIGNMENT); 
        buttons.setAlignmentX (Component.LEFT_ALIGNMENT);
        content.add (buttons);
        content.setBorder (new EmptyBorder (10, 10, 10, 10));
        
        final QPopup ep = UIUtils.createPopup ((title != null ? title : "Just so you know..."),
                                               Constants.INFO_ICON_NAME,
                                               content,
                                               true,
                                               null);

        close.addActionListener (new ActionListener ()
        {
            
            public void actionPerformed (ActionEvent ev)
            {
                
                ep.removeFromParent ();                
                                         
            }
        });

        content.setPreferredSize (new Dimension (DEFAULT_POPUP_WIDTH,
                                            content.getPreferredSize ().height));
                                            
        parent.showPopupAt (ep,
                            UIUtils.getCenterShowPosition ((Component) parent,
                                                           ep));
        ep.setDraggable ((Component) parent);

    }

    public static void showMessage (Component parent,
                                    String    message)
    {

        UIUtils.showMessage (parent,
                             null,
                             message);
    
    }    
 
    public static void showMessage (Component parent,
                                    String    title,
                                    String    message)
    {

        if (parent == null)
        {
            
            UIUtils.showMessage (title,
                                 message);
            
            return;
            
        }

        if (parent instanceof QuollPanel)
        {
            
            UIUtils.showMessage ((PopupsSupported) ((QuollPanel) parent).getProjectViewer (),
                                 title,
                                 message);
            
            return;
            
        }
        
        if (parent instanceof PopupsSupported)
        {
            
            UIUtils.showMessage ((PopupsSupported) parent,
                                 title,
                                 message);
            
            return;
            
        }
        
        if (parent instanceof PopupWindow)
        {
            
            UIUtils.showMessage (((PopupWindow) parent).getProjectViewer (),
                                 title,
                                 message);
            
            return;
            
        }
            
        UIUtils.showMessage (title,
                             message);

    }

    private static void showMessage (AbstractProjectViewer pv,
                                     String                title,
                                     String                message)
    {
        
        MessageWindow ew = new MessageWindow (pv,
                                              title,
                                              message);
        
        ew.init ();

    }
    
    private static void showMessage (String title,
                                     String message)
    {

        UIUtils.showMessage (null,
                             title,
                             message);

    }

    public static QPopup createPopup (String         title,
                                      String         icon,
                                      JComponent     content,
                                      boolean        includeCancel,                                      
                                      final ActionListener cancelListener)
    {
                
        final QPopup p = new QPopup (title,
                                     (icon != null ? Environment.getIcon (icon,
                                                                          Constants.ICON_POPUP) : null),
                                     null);
        
        if (content != null)
        {
        
            p.setContent (content);
            
        }
        
        if (includeCancel)
        {
            
            final JButton cancel = UIUtils.createButton ("cancel",
                                                         Constants.ICON_MENU,
                                                         "Click to close.",
                                                         null);
            
            cancel.addActionListener (new ActionAdapter ()
            {

                public void actionPerformed (ActionEvent ev)
                {

                    p.setVisible (false);
                    
                    if (cancelListener != null)
                    {
                        
                        cancelListener.actionPerformed (ev);
                        
                    }

                }

            });
            
            List<JButton> buts = new ArrayList ();
            buts.add (cancel);
            
            p.getHeader ().setControls (UIUtils.createButtonBar (buts));
            
        }

        return p;
        
    }

    public static JLabel createErrorLabel (String message)
    {
        
        JLabel err = new JLabel (message);
        err.setForeground (UIUtils.getColor (Constants.ERROR_TEXT_COLOR));
        err.setIcon (Environment.getIcon (Constants.ERROR_RED_ICON_NAME,
                                          Constants.ICON_MENU));
        err.setAlignmentX (Component.LEFT_ALIGNMENT);
        return err;
        
    }
    
    public static void createPopupMenuLabels (Map labels)
    {

        int maxWidth = 0;
        int maxHeight = 0;

        Iterator iter = labels.keySet ().iterator ();

        while (iter.hasNext ())
        {

            String l = (String) iter.next ();

            JLabel lab = new JLabel (l + " ");
            lab.setOpaque (false);
            lab.setBorder (null);

            labels.put (l,
                        lab);

            Dimension dim = lab.getPreferredSize ();

            int w = dim.width;
            int h = dim.height;

            if (w > maxWidth)
            {

                maxWidth = w;

            }

            if (h > maxHeight)
            {

                maxHeight = h;

            }

        }

        FormLayout fl = new FormLayout ("right:" + maxWidth + "px",
                                        "center:" + 20 + "px");

        iter = labels.entrySet ().iterator ();

        while (iter.hasNext ())
        {

            PanelBuilder    b = new PanelBuilder (fl);
            CellConstraints cc = new CellConstraints ();

            Map.Entry item = (Map.Entry) iter.next ();

            String l = (String) item.getKey ();

            JLabel ll = (JLabel) item.getValue ();

            b.add (ll,
                   cc.xy (1,
                          1));

            JPanel p = b.getPanel ();
            p.setOpaque (false);

            labels.put (l,
                        p);

        }

    }

    public static void setAsButton (Component c)
    {

        c.setCursor (Cursor.getPredefinedCursor (Cursor.HAND_CURSOR));

    }

    public static void setDefaultCursor (Component c)
    {

        c.setCursor (Cursor.getPredefinedCursor (Cursor.DEFAULT_CURSOR));

    }

    public static void setAsButton2 (Component c)
    {

        c.setCursor (Cursor.getPredefinedCursor (Cursor.HAND_CURSOR));

        if (c instanceof AbstractButton)
        {

            final AbstractButton b = (AbstractButton) c;

            b.setContentAreaFilled (false);

            // b.setMargin (new Insets (2, 2, 2, 2));
            b.addMouseListener (new MouseAdapter ()
                {

                    public void mouseEntered (MouseEvent ev)
                    {

                        b.setContentAreaFilled (true);

                    }

                    public void mouseExited (MouseEvent ev)
                    {

                        b.setContentAreaFilled (false);

                    }

                });

        }

    }

    public static Border createLineBorder ()
    {

        return new LineBorder (Environment.getBorderColor (),
                               1);

    }

    public static JComboBox getFontSizesComboBox (final int          sizeDef,
                                                  final TextStylable editor)
    {

        Vector<Integer> sizeV = new Vector ();

        boolean defAdded = false;

        for (int i = 8; i < 19; i += 2)
        {

            if ((sizeDef < i) &&
                (!defAdded))
            {

                sizeV.addElement (sizeDef);
                defAdded = true;

            }

            if (i != sizeDef)
            {

                sizeV.addElement (i);

            }

        }

        if (sizeDef > 18)
        {

            sizeV.addElement (sizeDef);

        }

        final JComboBox sizes = new JComboBox (sizeV);

        sizes.setEditor (new javax.swing.plaf.basic.BasicComboBoxEditor ()
            {

                protected JTextField createEditorComponent ()
                {

                    return new FormattedTextField ("[0-9]");

                }

            });

        sizes.setEditable (true);

        if (editor != null)
        {

            sizes.addActionListener (new ActionAdapter ()
                {

                    public void actionPerformed (ActionEvent ev)
                    {

                        try
                        {

                            // Need to take the screen resolution into account.
                            //editor.setFontSize (UIUtils.getEditorFontSize (Integer.parseInt (sizes.getSelectedItem ().toString ())));
                            editor.setFontSize (Integer.parseInt (sizes.getSelectedItem ().toString ()));
                            
                        } catch (Exception e)
                        {

                            // Ignore.

                        }

                    }

                });

        }

        sizes.setMaximumSize (sizes.getPreferredSize ());        
        
        if (sizeDef > 0)
        {

            sizes.setSelectedItem (sizeDef);

        }

        sizes.setMaximumSize (sizes.getPreferredSize ());

        sizes.setToolTipText ("Enter a value to set a size that is not already in the list");

        return sizes;

    }

    public static JComboBox getLineSpacingComboBox (final float        lsDef,
                                                    final TextStylable editor)
    {

        Vector<Float> lineS = new Vector ();

        boolean defAdded = false;

        for (float i = 0.5f; i < 2.5f; i += 0.5f)
        {

            if ((lsDef < i) &&
                (!defAdded))
            {

                lineS.addElement (lsDef);
                defAdded = true;

            }

            if (lsDef != i)
            {

                lineS.addElement (i);

            }

        }

        if (lsDef > 2.0f)
        {

            lineS.addElement (lsDef);

        }

        final JComboBox line = new JComboBox (lineS);

        line.setEditable (true);

        line.setEditor (new javax.swing.plaf.basic.BasicComboBoxEditor ()
            {

                protected JTextField createEditorComponent ()
                {

                    return new FormattedTextField ("[0-9\\.]");

                }

            });

        line.setToolTipText ("Enter a value to set a spacing that is not already in the list");

        if (editor != null)
        {

            line.addActionListener (new ActionAdapter ()
                {

                    public void actionPerformed (ActionEvent ev)
                    {

                        try
                        {

                            editor.setLineSpacing (Float.parseFloat (line.getSelectedItem ().toString ()));

                        } catch (Exception e)
                        {

                            // Ignore.

                        }

                    }

                });

        }

        line.setMaximumSize (line.getPreferredSize ());        
        
        if (lsDef > 0)
        {

            line.setSelectedItem (lsDef);

        }

        return line;

    }

    public static JComboBox getFontsComboBox (final String       selected,
                                              final TextStylable editor)
    {

        GraphicsEnvironment gEnv = GraphicsEnvironment.getLocalGraphicsEnvironment ();

        String[] envfonts = gEnv.getAvailableFontFamilyNames ();

        Vector<String> vector = new Vector ();

        for (int i = 1; i < envfonts.length; i++)
        {

            if (new Font (envfonts[i],
                          Font.PLAIN,
                          12).canDisplayUpTo ("ABCDEFabcdef") == -1)
            {

                vector.addElement (envfonts[i]);

            }

        }

        final JComboBox fonts = new JComboBox (vector);

        fonts.setRenderer (new DefaultListCellRenderer ()
            {

                public Component getListCellRendererComponent (JList   list,
                                                               Object  value,
                                                               int     index,
                                                               boolean isSelected,
                                                               boolean cellHasFocus)
                {

                    super.getListCellRendererComponent (list,
                                                        value,
                                                        index,
                                                        isSelected,
                                                        cellHasFocus);

                    this.setFont (new Font ((String) value,
                                            Font.PLAIN,
                                            12));

                    return this;

                }

            });

        fonts.setMaximumSize (fonts.getPreferredSize ());

        if (editor != null)
        {

            fonts.addActionListener (new ActionAdapter ()
                {

                    public void actionPerformed (ActionEvent ev)
                    {

                        fonts.setFont (new Font ((String) fonts.getSelectedItem (),
                                                 Font.PLAIN,
                                                 12));

                        editor.setFontFamily ((String) fonts.getSelectedItem ());

                        // _this.getEditor ().setFontSize (UIUtils.getEditorFontSize (Integer.parseInt (_this.sizes.getSelectedItem ().toString ())));

                    }

                });

        }

        if (selected != null)
        {

            fonts.setSelectedItem (selected);

        }

        return fonts;

    }

    public static JComboBox getAlignmentComboBox (final String       alignDef,
                                                  final TextStylable editor)
    {

        Vector<String> alignS = new Vector ();
        alignS.add ("Left");
        alignS.add ("Justified");
        alignS.add ("Right");

        final JComboBox align = new JComboBox (alignS);

        if (editor != null)
        {

            align.addActionListener (new ActionAdapter ()
                {

                    public void actionPerformed (ActionEvent ev)
                    {

                        editor.setAlignment ((String) align.getSelectedItem ());

                    }

                });

        }

        align.setMaximumSize (align.getPreferredSize ());
        
        if (alignDef != null)
        {

            align.setSelectedItem (alignDef);

        }

        return align;

    }

    public static Component getLimitWrapper (Component c)
    {

        Box sw = new Box (BoxLayout.X_AXIS);
        sw.add (c);
        sw.add (Box.createHorizontalGlue ());

        return sw;

    }

    public static Border createTestLineBorder ()
    {

        return new LineBorder (Color.GREEN,
                               1);

    }

    public static Border createTestLineBorder2 ()
    {

        return new LineBorder (Color.RED,
                               1);

    }

    public static Header createBoldSubHeader (String title,
                                              String iconType)
    {

        Header h = new Header (Environment.replaceObjectNames (title),
                               ((iconType == null) ? null : Environment.getIcon (iconType,
                                                                                 Constants.ICON_MENU)),
                               null);

        h.setFont (h.getFont ().deriveFont ((float) UIUtils.scaleToScreenSize (12)).deriveFont (Font.PLAIN));
                                            
        h.setTitleColor (UIUtils.getTitleColor ());
        h.setOpaque (false);

        // h.setBackground (new Color (0, 0, 0, 0));
        h.setPaintProvider (null);

        h.setAlignmentX (Component.LEFT_ALIGNMENT);

        h.setBorder (new CompoundBorder (new MatteBorder (0,
                                                          0,
                                                          0,
                                                          0,
                                                          Environment.getBorderColor ()),
                                         new EmptyBorder (0,
                                                          0,
                                                          2,
                                                          0)));

        // b.add (l);
/*
        b.add (Box.createHorizontalGlue ());

        b.setMaximumSize (new Dimension (Short.MAX_VALUE,
                                         Short.MAX_VALUE));
*/
        return h;

    }

    public static JLabel createSubHeader (String title,
                                          String iconType)
    {

        JLabel l = new JLabel (title);
        l.setIcon (Environment.getIcon (iconType,
                                        Constants.ICON_MENU));
        l.setMaximumSize (new Dimension (Short.MAX_VALUE,
                                         l.getPreferredSize ().height));
        l.setBorder (new CompoundBorder (new MatteBorder (0,
                                                          0,
                                                          1,
                                                          0,
                                                          Environment.getBorderColor ()),
                                         new EmptyBorder (0,
                                                          0,
                                                          2,
                                                          0)));
        l.setAlignmentX (Component.LEFT_ALIGNMENT);

        return l;

    }

/*
    public static List<Segment> getChapterSnippetsForNames (Collection<String> names,
                                                            Chapter            c)
    {

        return UIUtils.getTextSnippetsForNames (names,
                                                c.getText ());

    }
*/
    public static List<Segment> getTextSnippetsForNames (Collection<String> names,
                                                         String             text)
    {

        List<Segment> snippets = new ArrayList<Segment> ();

        if (text != null)
        {

            text = StringUtils.replaceString (text,
                                              String.valueOf ('\r'),
                                              "");

        }

        TextIterator ti = new TextIterator (text);

        for (String n : names)
        {
        
            Map<Sentence, NavigableSet<Integer>> matches = ti.findInSentences (n,
                                                                               null);
        
            if (matches != null)
            {
                
                Iterator<Sentence> iter = matches.keySet ().iterator ();
    
                char[] tchar = text.toCharArray ();
                
                while (iter.hasNext ())
                {
                    
                    Sentence sen = iter.next ();
                    
                    Set<Integer> inds = matches.get (sen);
                    
                    if ((inds != null)
                        &&
                        (inds.size () > 0)
                       )
                    {
                        
                        Segment s = new Segment (tchar,
                                                 sen.getAllTextStartOffset (),
                                                 sen.getText ().length ());
    
                        snippets.add (s);
    
                    }                
                    
                }
                
            }
            
        }
                
        /*
        Map<String, List<String>> nameWords = new HashMap ();

        // Tokenize it.
        BreakIterator bi = BreakIterator.getSentenceInstance ();

        bi.setText (text);

        int start = bi.first ();

        for (int end = bi.next (); end != BreakIterator.DONE; start = end, end = bi.next ())
        {

            String sentence = text.substring (start,
                                              end);

            String sentencel = sentence.toLowerCase ();

            List<String> sentencelWords = TextUtilities.getAsWords (sentencel);

            for (String name : names)
            {

                name = name.toLowerCase ();

                List<String> nw = nameWords.get (name);
                
                if (nw == null)
                {
                    
                    nw = TextUtilities.getAsWords (name);
                    
                    nameWords.put (name,
                                   nw);
                    
                }

                List<Integer> inds = null;
                
                try
                {
                    
                    inds = TextUtilities.indexOf (sentencelWords,
                                                  nw,
                                                  false,
                                                  new DialogueConstraints (false,
                                                                           false,
                                                                           null));
                
                } catch (Exception e) {
                    
                    // Ignore?
                    continue;
                    
                }

                if (inds.size () > 0)
                {

                    Segment s = new Segment (text.toCharArray (),
                                             start,
                                             sentence.length ());

                    snippets.add (s);

                    // Goto the next sentence.
                    break;

                }

            }

        }
*/
        return snippets;

    }

    public static Set<NamedObject> getObjectsContaining (String  s,
                                                         Project p)
    {

        Set<NamedObject> ret = new TreeSet (new NamedObjectSorter ());
        
        for (NamedObject n : p.getAllNamedChildObjects ())
        {
            
            if (n.contains (s))
            {
                
                ret.add (n);
                
            }
            
        }

        return ret;        
        
    }

    public static Set<Asset> getAssetsContaining (String  s,
                                                  Project p)
    {

        Set<Asset> ret = new TreeSet (new NamedObjectSorter ());
        
        for (NamedObject n : p.getAllNamedChildObjects (Asset.class))
        {
            
            if (n.contains (s))
            {
                
                ret.add ((Asset) n);
                
            }
            
        }

        return ret;        
        
    }

    public static Set<Asset> getAssetsContaining (String  s,
                                                  Class   limitTo,
                                                  Project p)
    {

        Set<Asset> ret = new TreeSet (new NamedObjectSorter ());
        
        for (NamedObject n : p.getAllNamedChildObjects (limitTo))
        {
            
            if (n.contains (s))
            {
                
                ret.add ((Asset) n);
                
            }
            
        }

        return ret;        
        
    }

    public static Set<Note> getNotesContaining (String  s,
                                                Project p)
    {

        Set<Note> ret = new TreeSet (new NamedObjectSorter ());
        
        for (NamedObject n : p.getAllNamedChildObjects (Note.class))
        {
            
            if (n.contains (s))
            {
                
                ret.add ((Note) n);
                
            }
            
        }

        return ret;        
        
    }

    public static Set<OutlineItem> getOutlineItemsContaining (String  s,
                                                              Project p)
    {

        Set<OutlineItem> ret = new TreeSet (new NamedObjectSorter ());
        
        for (NamedObject n : p.getAllNamedChildObjects (OutlineItem.class))
        {
            
            if (n.contains (s))
            {
                
                ret.add ((OutlineItem) n);
                
            }
            
        }

        return ret;        
        
    }

    public static Set<Scene> getScenesContaining (String  s,
                                                  Project p)
    {

        Set<Scene> ret = new TreeSet (new NamedObjectSorter ());
        
        for (NamedObject n : p.getAllNamedChildObjects (Scene.class))
        {
            
            if (n.contains (s))
            {
                
                ret.add ((Scene) n);
                
            }
            
        }

        return ret;        
        
    }

    public static Map<Chapter, List<Segment>> getObjectSnippets (NamedObject n,
                                                                 AbstractProjectViewer pv)
    {

        Project p = pv.getProject ();

        Map<Chapter, List<Segment>> data = new LinkedHashMap ();

        Set<String> names = n.getAllNames ();

        // String name = n.getName ().toLowerCase ();

        // Get all the books and chapters.
        List<Book> books = p.getBooks ();

        for (int i = 0; i < books.size (); i++)
        {

            Book b = books.get (i);

            List<Chapter> chapters = b.getChapters ();

            for (int j = 0; j < chapters.size (); j++)
            {

                Chapter c = chapters.get (j);

                String t = c.getText ();

                // See if there is an editor for it.
                AbstractEditorPanel aep = (AbstractEditorPanel) pv.getEditorForChapter (c);
                
                if (aep != null)
                {
                    
                    t = aep.getEditor ().getText ();
                    
                }                

                if ((t == null)
                    ||
                    (t.trim ().equals (""))
                   )
                {

                    continue;

                }

                List<Segment> snippets = UIUtils.getTextSnippetsForNames (names,
                                                                          t);

                if ((snippets != null) &&
                    (snippets.size () > 0))
                {

                    data.put (c,
                              snippets);

                }

            }

        }

        return data;

    }

    public static Map<Chapter, List<Segment>> getTextSnippets (String  s,
                                                               AbstractProjectViewer pv)
    {

        Map<Chapter, List<Segment>> data = new LinkedHashMap ();

        // String name = n.getName ().toLowerCase ();

        List<String> names = new ArrayList ();
        names.add (s);

        // Get all the books and chapters.
        List<Book> books = pv.getProject ().getBooks ();

        for (int i = 0; i < books.size (); i++)
        {

            Book b = books.get (i);

            List<Chapter> chapters = b.getChapters ();

            for (int j = 0; j < chapters.size (); j++)
            {

                Chapter c = chapters.get (j);

                AbstractEditorPanel qep = pv.getEditorForChapter (c);
                
                String t = null;
                
                if (qep != null)
                {
                    
                    t = qep.getEditor ().getText ();
                    
                } else {
                
                    // Get the text.
                    t = c.getText ();

                }
                    
                if (t == null)
                {

                    continue;

                }

                List<Segment> snippets = UIUtils.getTextSnippetsForNames (names,
                                                                          t);

                if ((snippets != null) &&
                    (snippets.size () > 0))
                {

                    data.put (c,
                              snippets);

                }

            }

        }

        return data;

    }

    public static String markupLinks (String s)
    {

        if (s == null)
        {

            return s;

        }

        s = Environment.replaceObjectNames (s);
                
        s = UIUtils.markupLinks ("http://",
                                 s);

        s = UIUtils.markupLinks ("https://",
                                 s);

        // Replace <a with "<a style=' etc...
        s = StringUtils.replaceString (s,
                                       "<a ",
                                       "<a style='color: " + Constants.HTML_LINK_COLOR + ";' ");

        return s;

    }

    public static String markupLinks (String urlPrefix,
                                      String s)
    {
        
        int ind = 0;        

        while (true)
        {

            ind = s.indexOf (urlPrefix,
                             ind);

            if (ind != -1)
            {

                // Now check the previous character to make sure it's not " or '.
                if (ind > 0)
                {

                    char c = s.charAt (ind - 1);

                    if ((c == '"') ||
                        (c == '\''))
                    {

                        ind += 1;

                        continue;

                    }

                }

                // Find the first whitespace char after...                
                char[] chars = s.toCharArray ();

                StringBuilder b = new StringBuilder ();

                for (int i = ind + urlPrefix.length (); i < chars.length; i++)
                {

                    if (!Character.isWhitespace (chars[i]))
                    {
                        
                        b.append (chars[i]);
                        
                    } else {
                        
                        break;
                        
                    }

                }

                // Now replace whatever we got...
                String st = b.toString ();

                if (st.length () == 0)
                {

                    continue;

                }

                // Not sure about this but "may" be ok.
                if ((st.endsWith (";")) ||
                    (st.endsWith (",")) ||
                    (st.endsWith (".")))
                {

                    st = st.substring (0,
                                       st.length () - 1);

                }
                
                String w = "<a href='" + urlPrefix + st + "'>" + urlPrefix + st + "</a>";

                StringBuilder ss = new StringBuilder (s);

                s = ss.replace (ind,
                                ind + st.length () + urlPrefix.length (),
                                w).toString ();

                ind = ind + w.length ();

            } else
            {

                // No more...
                break;

            }

        }

        return s;

    }

    public static void removeNodeFromTreeIfNoChildren (JTree  tree,
                                                       Object n)
    {

        DefaultTreeModel model = (DefaultTreeModel) tree.getModel ();

        DefaultMutableTreeNode root = (DefaultMutableTreeNode) model.getRoot ();

        // See how many children there are.
        TreePath tp = UIUtils.getTreePathForUserObject (root,
                                                        n);

        if (tp != null)
        {

            DefaultMutableTreeNode nNode = (DefaultMutableTreeNode) tp.getLastPathComponent ();

            if (nNode.getChildCount () == 0)
            {

                // Remove it.
                model.removeNodeFromParent (nNode);

            }

        }

    }

    public static void removeNodeFromTree (JTree  tree,
                                           Object n)
    {

        DefaultTreeModel model = (DefaultTreeModel) tree.getModel ();

        DefaultMutableTreeNode root = (DefaultMutableTreeNode) model.getRoot ();

        TreePath tp = UIUtils.getTreePathForUserObject (root,
                                                        n);
        
        // It can happen but shouldn't.
        if (tp == null)
        {
            
            return;
            
        }

        DefaultMutableTreeNode nNode = (DefaultMutableTreeNode) tp.getLastPathComponent ();

        if (nNode != null)
        {

            model.removeNodeFromParent (nNode);

        }

    }
/*
    public static Set<NamedObject> getReferencedObjects (String      t,
                                                         Project     p,
                                                         NamedObject ignore)
    {
        
        if (t == null)
        {
            
            return t;
            
        }

        Set<NamedObject> ret = new HashSet ();
    
        Set<NamedObject> objs = p.getAllNamedChildObjects (Asset.class);

        Set<NamedObject> chaps = p.getAllNamedChildObjects (Chapter.class);
        
        if (chaps.size () > 0)
        {
            
            objs.addAll (chaps);
            
        }

        if (objs.size () == 0)
        {
            
            return ret;
            
        }

        if (ignore != null)
        {

            objs.remove (ignore);

        }

        Map<String, List<NamedObjectNameWrapper>> items = new HashMap ();

        for (NamedObject o : objs)
        {

            NamedObjectNameWrapper.addTo (o,
                                          items);

        }

        // Get all the names.
        Set<String> names = items.keySet ();

        List<String> namesL = new ArrayList (names);

        try
        {

            JoSQLComparator jc = new JoSQLComparator ("SELECT * FROM java.lang.String ORDER BY toString.length DESC");

            Collections.sort (namesL,
                              jc);

        } catch (Exception e)
        {

            Environment.logError ("Unable to construct josql comparator",
                                  e);

        }

        for (String name : namesL)
        {

            int           ind = 0;
            int           start = 0;
            StringBuilder b = new StringBuilder ();
            String        tl = t.toLowerCase ();

            while (ind != -1)
            {

                ind = tl.indexOf (name,
                                  start);

                if (ind != -1)
                {

                    String tag = "[[_$@" + name.hashCode () + "@$_]]";

                    b.append (t.substring (start,
                                           ind));
                    b.append (tag);

                    start = ind + name.length ();

                } else
                {

                    b.append (t.substring (start));

                }

            }

            t = b.toString ();

        }

        for (String name : namesL)
        {

            List<NamedObjectNameWrapper> nitems = items.get (name);

            NamedObjectNameWrapper nw = nitems.get (0);

            t = StringUtils.replaceString (t,
                                           "[[_$@" + name.hashCode () + "@$_]]",
                                           "<a href='" + Constants.OBJECTREF_PROTOCOL + "://" + nw.namedObject.getObjectReference ().asString () + "'>" + nw.name + "</a>");

        }
        
        
    }
    */
    public static String markupStringForAssets (String      t,
                                                Project     p,
                                                NamedObject ignore)
    {

        if (t == null)
        {
            
            return t;
            
        }
    
        Set<NamedObject> objs = p.getAllNamedChildObjects (Asset.class);

        Set<NamedObject> chaps = p.getAllNamedChildObjects (Chapter.class);
        
        if (chaps.size () > 0)
        {
            
            objs.addAll (chaps);
            
        }

        if (objs.size () == 0)
        {
            
            return t;
            
        }

        if (ignore != null)
        {

            objs.remove (ignore);

        }

        Map<String, List<NamedObjectNameWrapper>> items = new HashMap ();

        for (NamedObject o : objs)
        {

            NamedObjectNameWrapper.addTo (o,
                                          items);

        }

        NavigableMap<Integer, NamedObjectNameWrapper> reps = new TreeMap ();
        
        TextIterator ti = new TextIterator (t);
                
        for (NamedObject n : objs)
        {
                        
            Set<Integer> matches = ti.findAllTextIndexes (n.getName (),
                                                          null);
            
            // This needs to be on a language basis.
            Set<Integer> matches2 = ti.findAllTextIndexes (n.getName () + "'s",
                                                           null);

            matches.addAll (matches2);
                                                           
            if (matches != null)
            {
                                                        
                Iterator<Integer> iter = matches.iterator ();

                while (iter.hasNext ())
                {
                    
                    Integer ind = iter.next ();
                    
                    // Check the char at the index, if it's uppercase then we upper the word otherwise lower.
                    if (Character.isLowerCase (t.charAt (ind)))
                    {
                        
                        reps.put (ind,
                                  new NamedObjectNameWrapper (n.getName ().toLowerCase (),
                                                              n));
                        
                    } else {
                        
                        // Uppercase each of the words in the name.
                        reps.put (ind,
                                  new NamedObjectNameWrapper (TextUtilities.capitalize (n.getName ()),
                                                              n));
                        
                    }

                }
                
            }            

        }
        
        StringBuilder b = new StringBuilder (t);
        
        // We iterate backwards over the indexes so we don't have to choppy-choppy the string.
        Iterator<Integer> iter = reps.descendingKeySet ().iterator ();
        
        while (iter.hasNext ())
        {
            
            Integer ind = iter.next ();
            
            NamedObjectNameWrapper obj = reps.get (ind);
            
            b = b.replace (ind,
                           ind + obj.name.length (),
                           "<a href='" + Constants.OBJECTREF_PROTOCOL + "://" + obj.namedObject.getObjectReference ().asString () + "'>" + obj.name + "</a>");
            
            
        }
        
        t = b.toString ();
        
        t = StringUtils.replaceString (t,
                                       String.valueOf ('\n'),
                                       "<br />");

        return t;

    }

    public static String getObjectALink (NamedObject d)
    {
        
        return "<a href='" + Constants.OBJECTREF_PROTOCOL + "://" + d.getObjectReference ().asString () + "'>" + d.getName () + "</a>";
        
    }
    
    public static Map<String, String> parseState (String s)
    {

        // Preserve the order, it "may" be important.
        Map ret = new LinkedHashMap ();

        StringTokenizer t = new StringTokenizer (s,
                                                 String.valueOf ('\n'));

        while (t.hasMoreTokens ())
        {

            String tok = t.nextToken ().trim ();

            StringTokenizer tt = new StringTokenizer (tok,
                                                      "=");

            while (tt.hasMoreTokens ())
            {

                if (tt.countTokens () == 2)
                {

                    String name = tt.nextToken ().trim ();
                    String value = tt.nextToken ().trim ();

                    ret.put (name,
                             value);

                }

            }

        }

        return ret;

    }

    public static String createState (Map values)
    {

        StringBuilder b = new StringBuilder ();

        Iterator iter = values.keySet ().iterator ();

        while (iter.hasNext ())
        {

            String k = iter.next ().toString ();
            String v = values.get (k).toString ();

            b.append (k);
            b.append ("=");
            b.append (v);
            b.append ('\n');

        }

        return b.toString ();

    }

    public static JButton createToolBarButton (String         icon,
                                               String         tooltip,
                                               String         actionCommand,
                                               ActionListener action)
    {

        JButton bt = new JButton (Environment.getIcon (icon,
                                                       Constants.ICON_TOOLBAR)); 
        UIUtils.setAsButton (bt);
        bt.setToolTipText (Environment.replaceObjectNames (tooltip));
        bt.setActionCommand (actionCommand);
        bt.setOpaque (false);

        if (action != null)
        {

            bt.addActionListener (action);

        }

        return bt;

    }

    public static JScrollPane createTreeScroll (JTree tree)
    {

        JScrollPane scroll = new JScrollPane (tree);
        scroll.setBorder (new MatteBorder (3,
                                           3,
                                           3,
                                           3,
                                           tree.getBackground ()));
        scroll.setOpaque (false);
        scroll.setOpaque (true);

        return scroll;

    }

    public static JTree createTree ()
    {

        JTree tree = new JTree ();
        tree.setCellRenderer (new ProjectTreeCellRenderer (false));
        tree.setOpaque (true);
        tree.setBorder (null);
        tree.setRootVisible (false);
        tree.setShowsRootHandles (true);
        tree.setScrollsOnExpand (true);
        tree.setRowHeight (0);

        tree.putClientProperty (com.jgoodies.looks.Options.TREE_LINE_STYLE_KEY,
                                com.jgoodies.looks.Options.TREE_LINE_STYLE_NONE_VALUE);

        tree.putClientProperty ("Tree.paintLines",
                                Boolean.FALSE);

        return tree;

    }

    public static void setTreeRootNode (JTree                  tree,
                                        DefaultMutableTreeNode tn)
    {
        
        java.util.List<TreePath> openPaths = new ArrayList ();

        Enumeration<TreePath> paths = tree.getExpandedDescendants (new TreePath (tree.getModel ().getRoot ()));

        if (paths != null)
        {

            while (paths.hasMoreElements ())
            {

                openPaths.add (paths.nextElement ());

            }

        }

        DefaultTreeModel dtm = (DefaultTreeModel) tree.getModel ();

        dtm.setRoot (tn);

        for (TreePath p : openPaths)
        {

            tree.expandPath (UIUtils.getTreePathForUserObject (tn,
                                                               ((DefaultMutableTreeNode) p.getLastPathComponent ()).getUserObject ()));

        }
        
    }

    public static Action getComingSoonAction (final Component pv)
    {

        return new ActionAdapter ()
        {

            public void actionPerformed (ActionEvent ev)
            {

                UIUtils.showMessage (pv,
                                     "This fantastic feature is coming soon!  Honest!");

            }

        };

    }

    public static void setCenterOfScreenLocation (Window f)
    {

        f.pack ();

        Dimension dim = Toolkit.getDefaultToolkit ().getScreenSize ();

        int w = f.getSize ().width;
        int h = f.getSize ().height;
        int x = (dim.width - w) / 2;
        int y = (dim.height - h) / 2;

        // Move the window
        f.setLocation (x,
                       y);

    }

    public static void addHyperLinkListener (final JEditorPane           p,
                                             final AbstractProjectViewer projectViewer)
    {

        p.addHyperlinkListener (new HyperlinkAdapter ()
            {

                public void hyperlinkUpdate (HyperlinkEvent ev)
                {

                    if (ev.getEventType () == HyperlinkEvent.EventType.ACTIVATED)
                    {

                        URL url = ev.getURL ();

                        UIUtils.openURL (projectViewer,
                                         url);

                    }

                }

            });

    }

    public static Header createHeader (String     title,
                                       int        titleType,
                                       String     icon,
                                       JComponent controls)
    {

        int iconType = Constants.ICON_MENU;
        int fontSize = 10;
        Insets ins = null;
                
        if (titleType == Constants.POPUP_WINDOW_TITLE)
        {
            
            iconType = Constants.ICON_POPUP;
            fontSize = 16;
            ins = null; //new Insets (3, 3, 3, 3);
            
        }
        
        if (titleType == Constants.SUB_PANEL_TITLE)
        {
            
            iconType = Constants.ICON_SUB_PANEL_MAIN;
            fontSize = 14;
            ins = new Insets (5, 5, 0, 0);
                               
        }

        if (titleType == Constants.PANEL_TITLE)
        {
            
            iconType = Constants.ICON_PANEL_MAIN;
            fontSize = 16;
            ins = new Insets (5, 7, 5, 7);

                               
        }

        if (titleType == Constants.FULL_SCREEN_TITLE)
        {
            
            iconType = Constants.ICON_PANEL_MAIN;
            fontSize = 18;
            ins = new Insets (5, 10, 8, 5);

                               
        }

        ImageIcon ii = null;
        
        if (icon != null)
        {
            
            ii = Environment.getIcon (icon,
                                      iconType);
            
        }
        
        Header h = new Header (Environment.replaceObjectNames (title),
                               ii,
                               controls);

        h.setAlignmentX (Component.LEFT_ALIGNMENT);

        h.setFont (h.getFont ().deriveFont ((float) UIUtils.scaleToScreenSize (fontSize)).deriveFont (Font.PLAIN));
        h.setTitleColor (UIUtils.getTitleColor ());
        h.setPadding (ins);

        return h;
        
    }
    
    public static JPanel createHelpBox (String                helpText,
                                        int                   iconType,
                                        AbstractProjectViewer pv)
    {

        FormLayout fl = new FormLayout ("p, 3px, fill:90px:grow",
                                        "top:p");

        PanelBuilder pb = new PanelBuilder (fl);

        CellConstraints cc = new CellConstraints ();
        
        ImagePanel helpII = new ImagePanel (Environment.getIcon ("help",
                                                                 iconType),
                                            null);
        helpII.setAlignmentX (Component.LEFT_ALIGNMENT);

        pb.add (helpII,
                cc.xy (1,
                       1));
        /*
        JTextArea helpT = new JTextArea ();
        helpT.setText (helpText);
        helpT.setEditable (false);
        helpT.setOpaque (false);
        helpT.setAlignmentX (Component.LEFT_ALIGNMENT);

        helpT.setWrapStyleWord (true);
        helpT.setLineWrap (true);
         */
        JTextPane helpT = UIUtils.createHelpTextPane (helpText,
                                                      pv);

        JScrollPane hsp = new JScrollPane (helpT);
        hsp.setOpaque (false);
        hsp.setBorder (null);
        hsp.getViewport ().setOpaque (false);
        hsp.setAlignmentX (Component.LEFT_ALIGNMENT);

        pb.add (hsp,
                cc.xy (3,
                       1));

        JPanel helpBox = pb.getPanel ();
        helpBox.setOpaque (false);
        helpBox.setVisible (false);
        helpBox.setBorder (new EmptyBorder (0,
                                            0,
                                            5,
                                            0));
        helpBox.setAlignmentX (Component.LEFT_ALIGNMENT);

        return helpBox;

    }

    public static JTextPane createHelpTextPane (String text,
                                                AbstractProjectViewer projectViewer)
    {

        HTMLEditorKit kit = new HTMLEditorKit ();
        HTMLDocument  doc = (HTMLDocument) kit.createDefaultDocument ();

        final JTextPane desc = new JTextPane (doc);

        desc.setEditorKit (kit);
        desc.setEditable (false);
        desc.setOpaque (false);
        desc.setAlignmentX (JComponent.LEFT_ALIGNMENT);

        desc.setSize (new Dimension (500,
                                     500));

        if (text != null)
        {

            desc.setText (UIUtils.getWithHTMLStyleSheet (desc,
                                                         text));

        }
        
        desc.setMaximumSize (new Dimension (500,
                                            500)); // desc.getPreferredSize ());

                                            /* old
        desc.setBorder (new EmptyBorder (10,
                                         5,
                                         10,
                                         5));
*/ 
                                         
        // new
        desc.setBorder (new EmptyBorder (5,
                                         5,
                                         10,
                                         5));
        // end new
                                         
        UIUtils.addHyperLinkListener (desc,
                                      projectViewer);

        return desc;

    }

    public static String getWithHTMLStyleSheet (JTextComponent desc,
                                                String         text)
    {

        return UIUtils.getWithHTMLStyleSheet (desc,
                                              text,
                                              null,
                                              null);

    }

    public static String getWithHTMLStyleSheet (JTextComponent desc,
                                                String         text,
                                                String         linkColor,
                                                String         textColor)
    {

        if (text == null)
        {
            
            text = "";
            
        }
    
        text = StringUtils.replaceString (text,
                                          String.valueOf ('\n'),
                                          "<br />");

        StringBuilder t = new StringBuilder ();
        t.append ("<html><head>");
        t.append (UIUtils.getHTMLStyleSheet (desc,
                                             linkColor,
                                             textColor));
        t.append ("</head><body><span>");
        t.append (UIUtils.markupLinks (text));
        t.append ("</span></body></html>");

        return t.toString ();

    }

    public static void openURL (Component parent,
                                String    url)
    {

        URL u = null;

        try
        {

            u = new URL (url);

        } catch (Exception e)
        {

            Environment.logError ("Unable to browse to: " +
                                  url,
                                  e);

            UIUtils.showErrorMessage (parent,
                                      "Unable to open web page: " + url);

            return;
                                      
        }

        UIUtils.openURL (parent,
                         u);

    }

    public static AbstractProjectViewer getProjectViewer (Component parent)
    {
        
        if (parent == null)
        {
            
            return null;
            
        }
        
        if (parent instanceof AbstractProjectViewer)
        {
            
            return (AbstractProjectViewer) parent;
            
        }
        
        if (parent instanceof PopupWindow)
        {
            
            return ((PopupWindow) parent).getProjectViewer ();
            
        }
        
        if (parent instanceof QuollPanel)
        {
            
            return ((QuollPanel) parent).getProjectViewer ();
            
        }
        
        return UIUtils.getProjectViewer (parent.getParent ());
        
    }

    public static void showFile (Component parent,
                                 File      f)
    {
        
        try
        {

            Desktop.getDesktop ().open (f);

        } catch (Exception e)
        {

            Environment.logError ("Unable to open: " +
                                  f,
                                  e);

            UIUtils.showErrorMessage (parent,
                                      "Unable to open: " + f);

            return;
                                      
        }

    }
    
    public static void openURL (Component parent,
                                URL       url)
    {

        if (url == null)
        {
            
            return;
            
        }
    
        if (url.getProtocol ().equals (Constants.QUOLLWRITER_PROTOCOL))
        {
            
            String u = Environment.getProperty (Constants.QUOLL_WRITER_WEBSITE_PROPERTY_NAME);

            String p = url.getPath ();

            if (!p.endsWith (".html"))
            {

                p += ".html";

            }

            u = u + "/" + p;
            
            if (url.getRef () != null)
            {
                
                u += "#" + url.getRef ();
                
            }

            try
            {

                url = new URL (u);

            } catch (Exception e)
            {

                Environment.logError ("Unable to open url: " +
                                      u,
                                      e);

                return;

            }            
            
        }
    
        if (url.getProtocol ().equals (Constants.HELP_PROTOCOL))
        {

            // Prefix it with the website.
            String u = Environment.getQuollWriterWebsite ();

            String p = url.getPath ();

            if (!p.endsWith (".html"))
            {

                p += ".html";

            }

            u = u + "/user-guide/" + url.getHost () + p;

            try
            {

                url = new URL (u);

            } catch (Exception e)
            {

                Environment.logError ("Unable to open url: " +
                                      u,
                                      e);

                return;

            }

            if (parent != null)
            {
                
                AbstractProjectViewer pv = UIUtils.getProjectViewer (parent);

                if (pv != null)
                {
                    
                    Environment.eventOccurred (new ProjectEvent (pv,
                                                                 ProjectEvent.HELP,
                                                                 ProjectEvent.SHOW));                    
                    
                }
                
            }
            
        }

        if (url.getProtocol ().equals (Constants.OBJECTREF_PROTOCOL))
        {

            if (parent != null)
            {
                
                AbstractProjectViewer pv = UIUtils.getProjectViewer (parent);

                if (pv != null)
                {

                    pv.viewObject (pv.getProject ().getObjectForReference (ObjectReference.parseObjectReference (url.getHost ())));

                    return;
                
                }
                
            }

        }
        
        
        if (url.getProtocol ().equals (Constants.ACTION_PROTOCOL))
        {

            String action = url.getPath ();
        
            if (parent != null)
            {
                
                AbstractProjectViewer pv = UIUtils.getProjectViewer (parent);

                if (pv != null)
                {
                    
                    pv.handleHTMLPanelAction (action);
                                        
                    return;
                
                }
                
            }
                            
        }
        
        if (url.getProtocol ().equals ("mailto"))
        {

            return;

        }

        try
        {

            Desktop.getDesktop ().browse (url.toURI ());

        } catch (Exception e)
        {

            Environment.logError ("Unable to browse to: " +
                                  url,
                                  e);

            UIUtils.showErrorMessage (parent,
                                      "Unable to open web page: " + url);

        }

    }

    public static JTextPane createObjectDescriptionViewPane (final String                description,
                                                             final NamedObject           n,
                                                             final AbstractProjectViewer pv,
                                                             final QuollPanel            qp)
    {
            
        HTMLEditorKit kit = new HTMLEditorKit ();
        HTMLDocument  doc = (HTMLDocument) kit.createDefaultDocument ();

        final JTextPane desc = new JTextPane (doc);

        desc.setEditorKit (kit);
        desc.setEditable (false);
        desc.setOpaque (false);
        /*
        desc.setMaximumSize (new Dimension (Short.MAX_VALUE,
                                            Short.MAX_VALUE));
*/
        desc.setSize (new Dimension (500, Short.MAX_VALUE));
        desc.addHyperlinkListener (new HyperlinkAdapter ()
            {

                public void hyperlinkUpdate (HyperlinkEvent ev)
                {

                    if (ev.getEventType () == HyperlinkEvent.EventType.ACTIVATED)
                    {

                        URL url = ev.getURL ();

                        if (url.getProtocol ().equals (Constants.OBJECTREF_PROTOCOL))
                        {

                            pv.viewObject (pv.getProject ().getObjectForReference (ObjectReference.parseObjectReference (url.getHost ())));

                            return;

                        }

                        if (url.getProtocol ().equals ("mailto"))
                        {

                            return;

                        }

                        UIUtils.openURL (pv,
                                         url);
                        
                    }

                }

            });

        Map events = new HashMap ();
        events.put (NamedObject.DESCRIPTION,
                    "");

        PropertyChangedAdapter pca = new PropertyChangedAdapter ()
        {

            public void propertyChanged (PropertyChangedEvent ev)
            {

                desc.setText (UIUtils.getWithHTMLStyleSheet (desc,
                                                             UIUtils.markupStringForAssets (description,
                                                                                            pv.getProject (),
                                                                                            n)));

            }

        };

        qp.addObjectPropertyChangedListener (pca,
                                             events);

        pca.propertyChanged (new PropertyChangedEvent (qp,
                                                       NamedObject.DESCRIPTION,
                                                       null,
                                                       null));

        return desc;

    }

    public static String colorToHex (Color c)
    {

        return "#" + Integer.toHexString (c.getRGB ()).substring (2);

    }
    public static String getHTMLStyleSheet (JTextComponent desc,
                                            String         textColor,
                                            String         linkColor)
    {

        return UIUtils.getHTMLStyleSheet (desc,
                                          textColor,
                                          linkColor,
                                          -1);
    
    }
    
    public static String getHTMLStyleSheet (JTextComponent desc,
                                            String         textColor,
                                            String         linkColor,
                                            int            textSize)
    {

        StringBuilder t = new StringBuilder ();

        Font f = null;
        
        if (desc != null)
        {
            
            f = desc.getFont ();
            
        } else {
            
            f = new JLabel ().getFont ();
            
        }

        if (linkColor == null)
        {
            
            linkColor = Constants.HTML_LINK_COLOR;
            
        }

        if (!linkColor.startsWith ("#"))
        {

            linkColor = "#" + linkColor;

        }

        if (textColor == null)
        {
            
            textColor = "#000000";
            
        }

        if (!textColor.startsWith ("#"))
        {

            textColor = "#" + textColor;

        }

        int size = textSize;
        
        if (size < 1)
        {
            
            size = (int) f.getSize ();
            
        }
        
        t.append ("<style>");
        t.append ("*{font-family: \"" + f.getFontName () + "\"; font-size: " + size + "px; background-color: transparent; color: " + textColor + ";}\n");
        t.append ("body{padding: 0px; margin: 0px;color: " + textColor + "; font-size: " + size + "pt; font-family: \"" + f.getFontName () + "\";}");
        t.append ("h1, h2, h3{font-family: \"" + f.getFontName () + "\";}\n");
        t.append ("span{color: " + textColor + "; font-size: " + size + "pt; font-family: \"" + f.getFontName () + "\";}\n");
        t.append ("p{padding: 0px; margin: 0px; color: " + textColor + "; font-size: " + size + "pt; font-family: \"" + f.getFontName () + "\";}\n");
        t.append ("img{vertical-align:middle; padding: 3px; border: solid 1px transparent; line-height: 22px;}");
        t.append ("a{color: " + linkColor + "; text-decoration: none; font-size: " + size + "pt; font-family: \"" + f.getFontName () + "\";}\n");
        t.append ("a.link{color: " + linkColor + "; text-decoration: none; font-size: " + size + "pt; font-family: \"" + f.getFontName () + "\";}\n");

        t.append ("a:hover {text-decoration: underline; font-size: " + size + "pt; font-family: \"" + f.getFontName () + "\";}\n");
        t.append ("b {font-size: " + size + "pt; font-family: \"" + f.getFontName () + "\";}\n");

        t.append ("ul{margin-left: 20px;}\n");
        t.append ("li{font-size: " + ((int) f.getSize ()) + "pt; font-family: \"" + f.getFontName () + "\";}\n");
        t.append ("p.error{padding: 0px; margin: 0px; color: red;}");
        t.append ("</style>");

        return t.toString ();

    }

    public static String getHTMLStyleSheet (JTextComponent desc)
    {

        return UIUtils.getHTMLStyleSheet (desc,
                                          "#000000",
                                          Constants.HTML_LINK_COLOR);

    }

    public static void addNewAssetItemsToPopupMenu (Container             m,
                                                    Component             showPopupAt,
                                                    AbstractProjectViewer pv,
                                                    String                name,
                                                    String                desc)
    {

        String pref = "Shortcut: Ctrl+Shift+";

        for (String type : Asset.supportedAssetTypes.keySet ())
        {

            Asset a = null;

            try
            {

                a = Asset.createSubType (type);

            } catch (Exception e)
            {

                Environment.logError ("Unable to create asset sub type: " +
                                      type,
                                      e);

                continue;

            }

            if (name != null)
            {
                
                a.setName (name);
                
            }

            if (desc != null)
            {
                
                a.setDescription (desc);
                
            }

            String oName = Environment.getObjectTypeName (a.getObjectType ());

            JMenuItem mi = new JMenuItem (oName,
                                          Environment.getIcon (a.getObjectType (),
                                                               Constants.ICON_MENU));

            char fc = Character.toUpperCase (oName.charAt (0));

            mi.setMnemonic (fc);
            mi.setToolTipText (pref + fc);

            AbstractActionHandler aah = new AssetActionHandler (a,
                                                                pv,
                                                                AbstractActionHandler.ADD);

            if (showPopupAt instanceof PopupsSupported)
            {

                aah.setPopupOver (pv);
                aah.setShowPopupAt (null,
                                    "below");

            } else
            {

                aah.setShowPopupAt (showPopupAt,
                                    "above");

            }

            mi.addActionListener (aah);

            m.add (mi);

        }

    }

    public static void addNewAssetItemsAsToolbarToPopupMenu (JPopupMenu    m,
                                                             Component     showPopupAt,
                                                             ProjectViewer pv,
                                                             String        name,
                                                             String        desc)
    {

        List<JComponent> buts = new ArrayList ();
    
        String pref = "Shortcut: Ctrl+Shift+";

        for (String type : Asset.supportedAssetTypes.keySet ())
        {

            Asset a = null;

            try
            {

                a = Asset.createSubType (type);

            } catch (Exception e)
            {

                Environment.logError ("Unable to create asset sub type: " +
                                      type,
                                      e);

                continue;

            }

            if (name != null)
            {
                
                a.setName (name);
                
            }

            if (desc != null)
            {
                
                a.setDescription (desc);
                
            }

            String oName = Environment.getObjectTypeName (a.getObjectType ());

            JButton but = UIUtils.createButton (Environment.getIcon (a.getObjectType (),
                                                                     Constants.ICON_MENU),
                                                "Click to add a new {" + a.getObjectType () + "}",
                                                null);

            AbstractActionHandler aah = new AssetActionHandler (a,
                                                                pv,
                                                                AbstractActionHandler.ADD);

            aah.setPopupOver (pv);
                                                                
            if (showPopupAt instanceof PopupsSupported)
            {

                aah.setShowPopupAt (null,
                                    "below");

            } else
            {

                aah.setShowPopupAt (showPopupAt,
                                    "above");

            }

            but.addActionListener (aah);

            buts.add (but);

        }

        m.add (UIUtils.createPopupMenuButtonBar (null,
                                                 m,
                                                 buts));
        
    }
    
    public static Color getColor (String hexCode)
    {

        if (hexCode.startsWith ("#"))
        {

            hexCode = hexCode.substring (1);

        }

        hexCode = hexCode.toUpperCase ();

        return new Color (Integer.parseInt (hexCode.substring (0,
                                                               2),
                                            16),
                          Integer.parseInt (hexCode.substring (2,
                                                               4),
                                            16),
                          Integer.parseInt (hexCode.substring (4),
                                            16));
    }

    public static int getWordCount (String text)
    {

        if (text == null)
        {

            return 0;

        }

        BreakIterator bi = BreakIterator.getWordInstance ();

        bi.setText (text);

        int wc = 0;

        int start = bi.first ();

        for (int end = bi.next (); end != BreakIterator.DONE; start = end, end = bi.next ())
        {

            String word = text.substring (start,
                                          end).trim ();

            if (word.equals (""))
            {

                continue;

            }

            // Check to make sure it's a word.
            char[] chars = word.toCharArray ();

            if ((!Character.isLetterOrDigit (chars[0])) ||
                (!Character.isLetterOrDigit (chars[chars.length - 1])))
            {

                continue;

            }

            wc++;

        }

        return wc;

    }

    public static int getSentenceCount (String text)
    {

        if (text == null)
        {

            return 0;

        }

        BreakIterator bi = BreakIterator.getSentenceInstance ();

        bi.setText (text);

        return UIUtils.getBreakIteratorCount (bi);

    }

    public static int getBreakIteratorCount (BreakIterator iter)
    {
        
        int wc = 0;

        int start = iter.first ();

        for (int end = iter.next (); end != BreakIterator.DONE; start = end, end = iter.next ())
        {

            wc++;

        }

        return wc;        
        
    }

    public static String getFrameTitle (String name)
    {

        return Environment.replaceObjectNames (name) + Environment.getWindowNameSuffix ();

    }

    public static void setFrameTitle (Frame  f,
                                      String name)
    {

        f.setTitle (UIUtils.getFrameTitle (name));

    }

    public static void setFrameTitle (Dialog f,
                                      String name)
    {

        f.setTitle (UIUtils.getFrameTitle (name));

    }

    public static JButton createHelpPageButton (final String  helpPage,
                                                final int     iconType,
                                                final String  helpText)
    {
        
        final JButton helpBut = new JButton (Environment.getIcon ("help",
                                                                  iconType));
        helpBut.setToolTipText ((helpText != null ? helpText : "Click to view the help"));
        helpBut.setOpaque (false);
        UIUtils.setAsButton (helpBut);

        helpBut.addActionListener (new ActionAdapter ()
        {
           
            public void actionPerformed (ActionEvent ev)
            {
                    
                UIUtils.openURL (helpBut,
                                 "help://" + helpPage);
                
            }
            
        });
        
        return helpBut;
        
    }

    public static JLabel createWebsiteLabel (final String  website,
                                             final String  display,
                                             final boolean useLabelText)
    {

        final JLabel web = new JLabel ("<html><u>" + ((display == null) ? website : display) + "</u></html>");

        // web.setEditable (false);
        web.setOpaque (false);
        web.setBorder (null);
        web.setForeground (new Color (112,
                                      149,
                                      226));
        web.setCursor (Cursor.getPredefinedCursor (Cursor.HAND_CURSOR));

        web.addMouseListener (new MouseAdapter ()
            {

                public void mouseEntered (MouseEvent ev)
                {

                    Map attrs = new HashMap ();
                    attrs.put (TextAttribute.UNDERLINE,
                               TextAttribute.UNDERLINE_LOW_ONE_PIXEL);

                    web.setFont (web.getFont ().deriveFont (attrs));

                }

                public void mouseExited (MouseEvent ev)
                {

                    Map attrs = new HashMap ();
                    attrs.put (TextAttribute.UNDERLINE,
                               null);

                    web.setFont (web.getFont ().deriveFont (attrs));

                }

                public void mouseClicked (MouseEvent ev)
                {

                    String w = website;

                    if (useLabelText)
                    {

                        w = web.getText ();

                    }

                    if ((w == null) ||
                        (w.trim ().equals ("")))
                    {

                        return;

                    }

                    if ((!w.toLowerCase ().startsWith ("http://")) &&
                        (!w.toLowerCase ().startsWith ("https://")))
                    {

                        w = "http://" + w;

                    }

                    try
                    {

                        UIUtils.openURL (web,
                                         w);

                    } catch (Exception e)
                    {

                        Environment.logError ("Unable to visit website: " +
                                              w,
                                              e);

                    }

                }

            });

        return web;

    }

    public static JLabel createClickableLabel (String title,
                                               Icon   icon)
    {

        final JLabel l = new JLabel (Environment.replaceObjectNames (title),
                                     icon,
                                     SwingConstants.LEFT);
        l.setForeground (UIUtils.getColor (Constants.HTML_LINK_COLOR));
        l.setCursor (Cursor.getPredefinedCursor (Cursor.HAND_CURSOR));
        l.addMouseListener (new MouseAdapter ()
        {

            public void mouseEntered (MouseEvent ev)
            {

                Map attrs = new HashMap ();
                attrs.put (TextAttribute.UNDERLINE,
                           TextAttribute.UNDERLINE_LOW_ONE_PIXEL);

                l.setFont (l.getFont ().deriveFont (attrs));

            }

            public void mouseExited (MouseEvent ev)
            {

                Map attrs = new HashMap ();
                attrs.put (TextAttribute.UNDERLINE,
                           null);

                l.setFont (l.getFont ().deriveFont (attrs));

            }

        });

        return l;

    }

    public static int getA4PageCountForChapter (Chapter c,
                                                String  text)
    {
        
        if ((text == null)
            ||
            (text.trim ().length () == 0)
           )
        {
            
            return 0;
            
        }
        
        // Create a new editor.
        QTextEditor ed = new QTextEditor (null,
                                          false,
                                          QuollEditorPanel.SECTION_BREAK);

        ed.setLineSpacing (c.getPropertyAsFloat (Constants.EDITOR_LINE_SPACING_PROPERTY_NAME));
        ed.setFontSize ((int) (UIUtils.getEditorFontSize (c.getPropertyAsInt (Constants.EDITOR_FONT_SIZE_PROPERTY_NAME))));
        ed.setFontFamily (c.getProperty (Constants.EDITOR_FONT_PROPERTY_NAME));
        ed.setAlignment (c.getProperty (Constants.EDITOR_ALIGNMENT_PROPERTY_NAME));

        ed.setText (text);

        int ppi = java.awt.Toolkit.getDefaultToolkit ().getScreenResolution ();

        // A4 - 17cm wide, 25.7cm high.
        // 6.7" wide, 10.12" high

        float pageHeight = 10.12f;
        float pageWidth = 6.7f;

        ed.setSize (new Dimension ((int) (pageWidth * ppi),
                                   Integer.MAX_VALUE));

        ed.setSize (new Dimension ((int) (pageWidth * ppi),
                                   ed.getPreferredSize ().height));

        // Get the height, divide by the page size.
        int a4PageCount = (int) (ed.getHeight () / (pageHeight * ppi));

        if (ed.getHeight () > (pageHeight * ppi * a4PageCount))
        {

            a4PageCount++;

        }

        /*
         *standard pages count
         
        ed.setSize (new Dimension (4 * ppi,
                                   ed.getPreferredSize ().height));

        int standardPageCount = ed.getHeight () / (4 * ppi);

        if (ed.getHeight () > (6.5f * ppi * standardPageCount))
        {

            standardPageCount++;

        }
        */
                
        return a4PageCount;        
        
    }
        
    public static ChapterCounts getChapterCounts (String  text)
    {

        ChapterCounts cc = new ChapterCounts ();

        if (text != null)
        {

            cc.wordCount = UIUtils.getWordCount (text);
            cc.wordFrequency = UIUtils.getWordFrequency (text);
            cc.sentenceCount = UIUtils.getSentenceCount (text);
            
        }

        return cc;

    }

    public static Map<String, Integer> getWordFrequency (String t)
    {
        
        Map<String, Integer> ret = new HashMap ();
        
        BreakIterator bi = BreakIterator.getWordInstance ();

        bi.setText (t);

        int start = bi.first ();

        for (int end = bi.next (); end != BreakIterator.DONE; start = end, end = bi.next ())
        {

            String word = t.substring (start,
                                       end).trim ();

            if (word.equals (""))
            {

                continue;

            }

            // Check to make sure it's a word.
            char[] chars = word.toCharArray ();

            if ((!Character.isLetterOrDigit (chars[0])) ||
                (!Character.isLetterOrDigit (chars[chars.length - 1])))
            {

                continue;

            }

            word = word.toLowerCase ();

            Integer wc = ret.get (word);
            
            int c = 0;
            
            if (wc != null)
            {
                
                c = wc.intValue ();
                
            }
            
            c++;

            ret.put (word,
                     Integer.valueOf (c));

        }
                
        return ret;
        
    }

    public static JScrollPane createScrollPane (JTextComponent t)
    {

        JScrollPane sp = new JScrollPane (t);
        sp.setOpaque (false);
        sp.setBorder (new CompoundBorder (new EmptyBorder (5,
                                                           5,
                                                           5,
                                                           5),
                                          UIUtils.createLineBorder ()));
        sp.setAlignmentX (Component.LEFT_ALIGNMENT);
        sp.getVerticalScrollBar ().setUnitIncrement (20);
        sp.setMaximumSize (new Dimension (Short.MAX_VALUE,
                                          t.getPreferredSize ().height + sp.getInsets ().top + sp.getInsets ().bottom));
        //sp.setPreferredSize (sp.getMaximumSize ());

        return sp;

    }

    public static JTextField createTextField ()
    {

        JTextField f = new JTextField ();
        f.setAlignmentX (JComponent.LEFT_ALIGNMENT);
        f.setBorder (new JScrollPane ().getBorder ());
        f.setMargin (new Insets (3,
                                 3,
                                 3,
                                 3));

        return f;

    }

    public static JPasswordField createPasswordField ()
    {

        JPasswordField f = new JPasswordField ();
        f.setAlignmentX (JComponent.LEFT_ALIGNMENT);
        f.setBorder (new JScrollPane ().getBorder ());
        f.setMargin (new Insets (3,
                                 3,
                                 3,
                                 3));

        return f;

    }

    public static JComponent createOpaqueGlue (int dir)
    {
        
        JComponent c = null;
        
        if (dir == BoxLayout.Y_AXIS)
        {
            
            c = (JComponent) Box.createVerticalGlue ();
            
        } else {
            
            c = (JComponent) Box.createHorizontalGlue ();
            
        }
        
        c.setOpaque (true);
        c.setBackground (UIUtils.getComponentColor ());
        c.setBorder (UIUtils.createTestLineBorder ());
        return c;
        
    }
    
    public static JScrollPane createScrollableTextArea (int rows)
    {

        return UIUtils.createScrollPane (UIUtils.createTextArea (rows));
        
    }
    
    public static JTextArea createTextArea (int rows)
    {

        JTextArea t = new JTextArea ();
        t.setFocusTraversalKeys (KeyboardFocusManager.FORWARD_TRAVERSAL_KEYS,
                                 null);
        t.setFocusTraversalKeys (KeyboardFocusManager.BACKWARD_TRAVERSAL_KEYS,
                                 null);
        t.setAlignmentX (JComponent.LEFT_ALIGNMENT);

        if (rows > 0)
        {

            t.setRows (rows);

        }

        t.setMargin (new Insets (3,
                                 3,
                                 3,
                                 3));
        t.setLineWrap (true);
        t.setWrapStyleWord (true);

        t.setSize (new Dimension (500,
                                  t.getPreferredSize ().height));
        
        return t;

    }

    public static JComponent createPopupMenuButtonBar (String           title,
                                                       final JPopupMenu       parent,
                                                       List<JComponent> buttons)
    {
        
        ActionListener aa = new ActionAdapter ()
        {
            
            public void actionPerformed (ActionEvent ev)
            {
                
                parent.setVisible (false);
                
            }
            
        };
        
        for (JComponent but : buttons)
        {
            
            if (but instanceof JButton)
            {
                
                JButton b = (JButton) but;
                
                b.addActionListener (aa);
                
            }
            
        }
        
        Box b = new Box (BoxLayout.X_AXIS);
        
        // This is to get around the "icon space" in the menu.
        b.add (Box.createHorizontalStrut (32));
        
        if (title == null)
        {
            
            title = "";
            
        }
            
        JLabel l = new JLabel (title);

        l.setForeground (UIUtils.getColor ("#444444"));
        l.setFont (l.getFont ().deriveFont (Font.ITALIC));
        
        l.setPreferredSize (new Dimension (Math.max (50, l.getPreferredSize ().width),
                                           l.getPreferredSize ().height));
                    
        b.add (l);
        
        b.add (Box.createHorizontalStrut (10));
                                              
        b.add (UIUtils.createButtonBar (buttons));
                
        b.add (Box.createHorizontalGlue ());
                
        return b;
        
    }
    
    public static JPanel createButtonBar2 (JButton[] buts,
                                           float     alignment)
    {
        
        ButtonBarBuilder bb = ButtonBarBuilder.create ();
        
        if (buts == null)
        {
            
            return bb.build ();
            
        }        
        
        if ((alignment == Component.RIGHT_ALIGNMENT)
            ||
            (alignment == Component.CENTER_ALIGNMENT)
           )
        {
         
            bb.addGlue ();
            
        }
        
        bb.addButton (buts);
        
        if (alignment == Component.CENTER_ALIGNMENT)
        {
            
            bb.addGlue ();
            
        }
        
        bb.opaque (false);

        JPanel p = bb.build ();
        
        p.setAlignmentX (Component.LEFT_ALIGNMENT);
        
        return p;
        
    }
    
    public static JToolBar createButtonBar (List<? extends JComponent> buttons)
    {

        JToolBar tb = new JToolBar ();
        tb.setOpaque (false);
        tb.setFloatable (false);
        tb.setRollover (true);
        tb.setBackground (new Color (0,
                                     0,
                                     0,
                                     0));

        for (int i = 0; i < buttons.size (); i++)
        {

            JComponent b = buttons.get (i);

            UIUtils.setAsButton2 (b);
            tb.add (b);

        }

        tb.setBackground (null);

        return tb;

    }

    public static JButton createButton (ImageIcon      icon,
                                        String         toolTipText,
                                        ActionListener action)
    {

        JButton b = new JButton (icon);
        b.setToolTipText (Environment.replaceObjectNames (toolTipText));
        b.setOpaque (false);
        UIUtils.setAsButton (b);
        //b.setFocusable (false);
        //b.setFocusPainted (false);
        
        if (action != null)
        {
        
            b.addActionListener (action);
            
        }
        
        return b;

    }

    public static JButton createButton (String         icon,
                                        int            iconType,
                                        String         toolTipText,
                                        ActionListener action)
    {
 
        return UIUtils.createButton (Environment.getIcon (icon,
                                                          iconType),
                                     toolTipText,
                                     action);
        
    }

    public static JButton createButton (String label,
                                        String icon)
    {

        JButton b = new JButton (Environment.replaceObjectNames (label),
                                 (icon == null ? null : Environment.getIcon (icon,
                                                                             Constants.ICON_MENU)));

        return b;

    }

    public static void treeChanged (DataObject d,
                                    JTree      tree)
    {

        DefaultTreeModel model = (DefaultTreeModel) tree.getModel ();

        DefaultMutableTreeNode node = (DefaultMutableTreeNode) UIUtils.getTreePathForUserObject ((DefaultMutableTreeNode) model.getRoot (),
                                                                                                 d).getLastPathComponent ();

        model.nodeStructureChanged (node);

    }

    public static void informTreeOfNodeChange (NamedObject n,
                                               JTree       tree)
    {

        DefaultTreeModel model = (DefaultTreeModel) tree.getModel ();

        DefaultMutableTreeNode node = (DefaultMutableTreeNode) UIUtils.getTreePathForUserObject ((DefaultMutableTreeNode) model.getRoot (),
                                                                                                 n).getLastPathComponent ();

        model.nodeChanged (node);

    }

    public static String formatPrompt (Prompt p)
    {

        if (p == null)
        {
            
            return "Prompt no longer available.  Usually this is due to it's removal at the request of the author.";
            
        }
    
        String link = "";

        if (p.isUserPrompt ())
        {

            link = "by You";

        } else
        {

            link = "<a title='Click to visit the website' href='" + p.getURL () + "'>" + p.getStoryName () + " by " + p.getAuthor () + "</a></span>";

        }

        return p.getText () + " - " + link;


    }

    public static Font getHeaderFont ()
    {

        if (UIUtils.headerFont == null)
        {
            
            UIUtils.headerFont = new JLabel ("").getFont ().deriveFont (Font.BOLD,
                                                                        16);
            
        }
            
        return UIUtils.headerFont;

    }

    public static int scaleToScreenSize (double h)
    {
        
        return Math.round ((float) h * ((float) java.awt.Toolkit.getDefaultToolkit ().getScreenResolution () / 72f));
        
    }

    public static int getPrintFontSize ()
    {
        
        return UIUtils.getPrintFontSize (new JLabel ().getFont ().getSize ());
        
    }
    
    public static int getPrintFontSize (int size)
    {

        return Math.round ((float) size / ((float) java.awt.Toolkit.getDefaultToolkit ().getScreenResolution () / 72f));

    }

    public static int getEditorFontSize (int size)
    {

        // Need to take the screen resolution into account.
        float s = (float) size * ((float) java.awt.Toolkit.getDefaultToolkit ().getScreenResolution () / 72f);

        return (int) s;

    }

    public static JFreeChart createSparkLine (TimeSeries series,
                                              int        maxRange,
                                              int        minRange)
    {

        TimeSeriesCollection tsc = new TimeSeriesCollection ();
        tsc.addSeries (series);

        DateAxis x = new DateAxis ();
        x.setTickUnit (new DateTickUnit (DateTickUnitType.DAY,
                                         1));
        x.setTickLabelsVisible (false);
        x.setTickMarksVisible (false);
        x.setAxisLineVisible (false);
        x.setNegativeArrowVisible (false);
        x.setPositiveArrowVisible (false);
        x.setVisible (false);

        NumberAxis y = new NumberAxis ();
        y.setTickLabelsVisible (false);
        y.setTickMarksVisible (false);
        y.setAxisLineVisible (false);
        y.setNegativeArrowVisible (false);
        y.setPositiveArrowVisible (false);
        y.setVisible (false);
        y.setRange (minRange,
                    maxRange);

        XYPlot plot = new XYPlot ();
        plot.setInsets (new RectangleInsets (-1,
                                             -1,
                                             0,
                                             0));
        plot.setDataset (tsc);
        plot.setDomainAxis (x);
        plot.setDomainGridlinesVisible (false);
        plot.setDomainCrosshairVisible (false);
        plot.setRangeGridlinesVisible (false);
        plot.setRangeCrosshairVisible (false);
        plot.setRangeAxis (y);

        XYLineAndShapeRenderer rend = new XYLineAndShapeRenderer (true,
                                                                  false);
        plot.setRenderer (rend);
        rend.setBaseStroke (new java.awt.BasicStroke (10f));
        rend.setSeriesStroke (0,
                              new java.awt.BasicStroke (10f));
        rend.setBasePaint (UIUtils.getColor ("#516CA3"));

        rend.setSeriesPaint (0,
                             UIUtils.getColor ("#516CA3"));

        JFreeChart chart = new JFreeChart (null,
                                           JFreeChart.DEFAULT_TITLE_FONT,
                                           plot,
                                           false);
        chart.setBorderVisible (false);

        return chart;

    }

    public static boolean isChildOf (MouseEvent ev,
                                     Component  parent)
    {

        return UIUtils.isChildOf ((Component) ev.getSource (),
                                  parent);

    }

    public static boolean isChildOf (Component child,
                                     Component parent)
    {

        Container p = child.getParent ();

        while (p != null)
        {

            if (p == parent)
            {

                return true;

            }

            p = p.getParent ();

        }

        return false;

    }
    
    public static ImageIcon getColoredIcon (String name,
                                            int    type,
                                            Color  c)
    {
        
        ImageIcon ic = Environment.getIcon (name,
                                            type);
        
        if (ic == null)
        {
            
            return null;
            
        }
        
        return UIUtils.getColoredIcon (ic.getImage (),
                                       c);
                
    }

    public static ImageIcon getColoredIcon (Image  im,
                                            Color  c)
    {
        
        if (im == null)
        {
            
            return null;
            
        }
        
        final int crgb = c.getRGB ();
        
        ImageFilter filter = new RGBImageFilter()
        {
          public final int filterRGB(int x, int y, int rgb)
          {
            if (rgb != 0)
            {
            
            }
             return (rgb != 0 ? crgb : rgb);

          }
        };
    
        ImageProducer ip = new FilteredImageSource (im.getSource (), filter);
        return new ImageIcon (Toolkit.getDefaultToolkit().createImage(ip)); 
        
    }
    
    public static Color getIconColumnColor ()
    {
        
        return UIUtils.getColor ("#f0f0f0");//"f5fcfe");        
        
    }

    public static int getSplitPaneDividerSize ()
    {
        
        return 7;
        
    }
    
    public static Color getTitleColor ()
    {
        
        return UIUtils.getColor ("#333333");
        
    }
    
    public static Color getInnerBorderColor ()
    {
    
        return UIUtils.getColor ("#dddddd");
        
    }
    
    public static Color getDragIconColor ()
    {
        
        return UIUtils.getColor ("#aaaaaa");
        
    }
    
    public static Color getBorderColor ()
    {
        
        return UIUtils.getColor ("#aaaaaa");        
        
    }
    
    public static Color getComponentColor ()
    {
        
        return UIUtils.getColor ("#fdfdfd");
        
    }
    
    public static boolean clipboardHasContent ()
    {

        try
        {

            return (Toolkit.getDefaultToolkit ().getSystemClipboard ().getData (DataFlavor.stringFlavor) != null);

        } catch (Exception e)
        {

        }

        return false;

    }

    public static void addListenerToChildren (MouseListener l,
                                              Container     parent)
    {

        parent.addMouseListener (l);

        int n = parent.getComponentCount ();

        for (int i = 0; i < n; i++)
        {

            Component c = parent.getComponent (i);

            c.addMouseListener (l);

            if (c instanceof Container)
            {

                UIUtils.addListenerToChildren (l,
                                               (Container) c);

            }

        }

    }

    public static BufferedImage getBufferedImage (Image     im,
                                                  Component io)
    {
        
        if (im == null)
        {
            
            return null;
            
        }
        
        int           height = im.getHeight (io);
        int           width = im.getWidth (io);

        BufferedImage bi = new BufferedImage (width,
                                              height,
                                              BufferedImage.TYPE_INT_ARGB);
        Graphics    g = bi.getGraphics ();
        g.drawImage (im,
                     0,
                     0,
                     Color.black,
                     io);
        
        return bi;
        
    }
    
    public static BufferedImage getScaledImage (DataSource ds,
                                                int        width,
                                                int        height)
    {
        
        try
        {
            
            return UIUtils.getScaledImage (ImageIO.read (ds.getInputStream ()),
                                           width,
                                           height);
            
        } catch (Exception e) {
            
            Environment.logError ("Unable to read image from data source: " + ds,
                                  e);
            
            return null;            
            
        }
        
    }
    
    public static BufferedImage getImage (File f)
    {
        
        try
        {
        
            return ImageIO.read (f);
        
        } catch (Exception e) {
            
            Environment.logError ("Unable to find image for file: " + f,
                                  e);
            
            return null;
            
        }
                
    }

    public static BufferedImage getScaledImage (File          f,
                                                int           width,
                                                int           height)
    {
        
        return UIUtils.getScaledImage (UIUtils.getImage (f),
                                       width,
                                       height);
        
    }

    public static BufferedImage getScaledImage (File          f,
                                                int           width)
    {
        
        return UIUtils.getScaledImage (UIUtils.getImage (f),
                                       width);
        
    }

    public static BufferedImage getScaledImage (BufferedImage img,
                                                int           targetWidth)
    {

        return Scalr.resize (img,
                             Scalr.Method.QUALITY,
                             Scalr.Mode.FIT_TO_WIDTH,
                             targetWidth,
                             Scalr.OP_ANTIALIAS);
    
    }
    
    public static Graphics2D createThrowawayGraphicsInstance ()
    {
        
        return new BufferedImage (1, 1, BufferedImage.TYPE_INT_ARGB).createGraphics ();
        
    }
    
    public static BufferedImage getScaledImage (BufferedImage img,
                                                int           targetWidth,
                                                int           targetHeight)
    {

        return Scalr.resize (img,
                             Scalr.Method.QUALITY,
                             Scalr.Mode.FIT_EXACT,
                             (targetWidth > 0 ? targetWidth : img.getWidth ()),
                             (targetHeight > 0 ? targetHeight : img.getHeight ()),
                             Scalr.OP_ANTIALIAS);
/*    
        BufferedImage tmp = new BufferedImage (targetWidth, targetHeight, BufferedImage.TYPE_INT_ARGB);
        Graphics2D g2 = tmp.createGraphics();
        g2.setRenderingHint (RenderingHints.KEY_RENDERING,
                             RenderingHints.VALUE_RENDER_QUALITY);
        g2.drawImage(img, 0, 0, targetWidth, targetHeight, null);
        g2.dispose();

        return tmp;
  */  
    }    

    public static ImageIcon overlayImage (ImageIcon bg,
                                          ImageIcon fg,
                                          String    where)
    {

        JButton but = new JButton ();

        Image bgi = bg.getImage ();
        Image fgi = fg.getImage ();
                
        BufferedImage n = new BufferedImage (bgi.getWidth (but),
                                             bgi.getHeight (but),
                                             BufferedImage.TYPE_INT_ARGB);
                                
        Graphics2D g = n.createGraphics ();
        g.setRenderingHint (RenderingHints.KEY_ANTIALIASING,
                            RenderingHints.VALUE_ANTIALIAS_ON);
                
        g.drawImage (bgi,
                     0,
                     0,
                     null);
        
        if (where.equals ("tl"))
        {
            
            g.drawImage (fgi,
                         0,
                         0,
                         null);
            
        }

        if (where.equals ("tr"))
        {
            
            g.drawImage (fgi,
                         bgi.getWidth (but) - fgi.getWidth (but),
                         0,
                         null);
            
        }

        if (where.equals ("bl"))
        {
            
            g.drawImage (fgi,
                         0,
                         bgi.getHeight (but) - fgi.getHeight (but),
                         null);
            
        }

        if (where.equals ("br"))
        {
            
            g.drawImage (fgi,
                         bgi.getWidth (but) - fgi.getWidth (but),
                         bgi.getHeight (but) - fgi.getHeight (but),
                         null);
            
        }
        
        g.dispose ();
        
        //bg.setImage (bgi);
        
        return new ImageIcon (n);
        
    }

    public static FileFinder createFileFind (final String         initPath,
                                             final String         finderTitle,
                                             final int            fileSelectionMode,
                                             final String         approveButtonText,
                                             final ActionListener handler)
    {
        
        FileFinder ff = new FileFinder ();
        
        if (initPath != null)
        {
        
            ff.setFile (new File (initPath));
            
        }
        
        ff.setOnSelectHandler (handler);
        ff.setApproveButtonText (approveButtonText);
        ff.setFinderSelectionMode (fileSelectionMode);
        ff.setFinderTitle (finderTitle);
        
        ff.init ();
        
        return ff;
        
    }

    public static Component getComponentByName (Container parent,
                                                String    name)
    {
        
        if (name == null)
        {
            
            return null;
            
        }
        
        if (parent == null)
        {
            
            return null;
            
        }
        
        Component[] children = parent.getComponents ();
        
        for (int i = 0; i < children.length; i++)
        {
            
            if (name.equals (children[i].getName ()))
            {
                
                return children[i];
                
            }
            
        }
        
        return null;
        
    }
    
    public static String formatForUser (String v)
    {
        
        v = Environment.replaceObjectNames (v);
        
        return v;
        
    }

    public static JMenuItem createMenuItem (String label,
                                            String icon,
                                            ActionListener action)
    {
        
        JMenuItem mi = new JMenuItem (Environment.replaceObjectNames (label),
                                      Environment.getIcon (icon,
                                                 Constants.ICON_MENU));
        mi.addActionListener (action);
        
        return mi;
        
    }    
    
    public static JMenuItem createMenuItem (String label,
                                            String icon,
                                            ActionListener action,
                                            String         actionCommand,
                                            KeyStroke      accel)
    {
        
        JMenuItem mi = UIUtils.createMenuItem (label,
                                               icon,
                                               action);
        
        mi.setActionCommand (actionCommand);
        mi.setAccelerator (accel);
        
        return mi;
        
    }
    
    public static void listenToTableForCellChanges (JTable table,
                                                    Action action)
    {
        
        // Shouldn't be cleaned up since it adds itself as a property listener to the table.
        new TableCellListener (table,
                               action);
        
    }

    public static boolean isAnimatedGif (File f)
    {
        
        String type = Utils.getFileType (f);
        
        if (f == null)
        {
            
            throw new IllegalArgumentException ("Unable to determine file type for file: " +
                                                f);
            
        }
        
        int c = 0;
        
        ImageReader r = null;
        
            r = ImageIO.getImageReadersBySuffix (type).next ();
            
            if (r == null)
            {
                
                throw new IllegalArgumentException ("Unsupported file type for file: " +
                                                    f);
                
            }
                 
        try
        {
                 
            r.setInput (ImageIO.createImageInputStream (f));
            
        } catch (Exception e) {
            
            throw new IllegalArgumentException ("Unable to read image file: " +
                                                f,
                                                e);            
                        
        }
        
        try
        {
                        
            int ind = 0;
            
            // Upper bound valid?
            while (ind < 100)
            {

                if (c > 1)
                {
                    
                    return true;
                    
                }
                
                r.read (ind);
                
                ind++;
                c++;
                
            }
            
        } catch (Exception e) {
            
        }

        return false;
        
    }

    public static void doActionWhenPanelIsReady (final QuollPanel     p,
                                                 final ActionListener action,
                                                 final Object         contextObject,
                                                 final String         actionTypeName)
    {
        
        final javax.swing.Timer t = new javax.swing.Timer (100,
                                                           null);   
        
        ActionListener l = new ActionListener ()
        {
            
            private int count = 0;
            
            public void actionPerformed (ActionEvent ev)
            {
                
                if (p.isReadyForUse ())
                {
                    
                    t.setRepeats (false);
                    t.stop ();

                    try
                    {
                        
                        action.actionPerformed (new ActionEvent ((contextObject != null ? contextObject : action),
                                                                 1,
                                                                 (actionTypeName != null ? actionTypeName : "any")));
                        
                    } catch (Exception e) {
                        
                        Environment.logError ("Unable to perform action",
                                              e);
            
                        UIUtils.showErrorMessage ("Sorry, the action cannot be performed, please contact Quoll Writer support for assistance.");
                                                
                    }
                                
                    return;
                    
                }

                // 2s delay max.
                if (count > 20)
                {
                    
                    Environment.logError ("Unable to perform action",
                                          new Exception ("Unable to perform action for panel: " + p.getPanelId ()));
        
                    UIUtils.showErrorMessage ("Sorry, the action cannot be performed, please contact Quoll Writer support for assistance.");                    
                    
                    t.setRepeats (false);
                    t.stop ();
                    
                }
                
                count++;
                                
            }
            
        };
        
        t.setRepeats (true);
        t.addActionListener (l);
        
        t.start ();
        
    }
    
    public static void doActionLater (final ActionListener action)
    {
        
        UIUtils.doActionLater (action,
                               null,
                               null);
        
    }
    
    public static void doActionLater (final ActionListener action,
                                      final Object         contextObject,
                                      final String         actionTypeName)
    {
        
        SwingUtilities.invokeLater (new Runner ()
        {
           
            public void run ()
            {
                
                try
                {
                    
                    action.actionPerformed (new ActionEvent ((contextObject != null ? contextObject : action),
                                                             1,
                                                             (actionTypeName != null ? actionTypeName : "any")));
                    
                } catch (Exception e) {
                    
                    Environment.logError ("Unable to perform action",
                                          e);
        
                    UIUtils.showErrorMessage ("Sorry, the action cannot be performed, please contact Quoll Writer support for assistance.");
                                            
                }
                
            }
            
        });
        
    }
    
    public static void addDoActionOnReturnPressed (final JTextComponent text,
                                                    final ActionListener action)
    {
        
        if (text instanceof JTextField)
        {
            
            text.addKeyListener (new KeyAdapter ()
            {

                public void keyPressed (KeyEvent ev)
                {

                    if (ev.getKeyCode () == KeyEvent.VK_ENTER)
                    {

                        // This is the same as save for the form.
                        action.actionPerformed (new ActionEvent (text,
                                                                 1,
                                                                 "return-pressed"));

                    }

                }

            });            
            
        } else {
        
            text.addKeyListener (new KeyAdapter ()
            {
    
                public void keyPressed (KeyEvent ev)
                {
    
                    if (((ev.getModifiersEx () & InputEvent.CTRL_DOWN_MASK) == InputEvent.CTRL_DOWN_MASK) &&
                        (ev.getKeyCode () == KeyEvent.VK_ENTER))
                    {
    
                        action.actionPerformed (new ActionEvent (text,
                                                                 1,
                                                                 "ctrl-return-pressed"));
    
                    }
    
                }
    
            });        

        }
        
    }

    public static MessageWindow createQuestionPopup (PopupWindow           popup,
                                                     String                title,
                                                     String                icon,
                                                     String                message,
                                                     final String          confirmButtonLabel,
                                                     final String          cancelButtonLabel,
                                                     final ActionListener  onConfirm,
                                                     final ActionListener  onCancel,
                                                     Point                 showAt)
    {
        
        Map<String, ActionListener> buttons = new LinkedHashMap ();
                
        if (onConfirm != null)
        {
                
            buttons.put (Environment.getButtonLabel (confirmButtonLabel,
                                                     Constants.CONFIRM_BUTTON_LABEL_ID),
                         new ActionAdapter ()
            {
                
                public void actionPerformed (ActionEvent ev)
                {
                                            
                    try
                    {
                        
                        onConfirm.actionPerformed (ev);
                        
                    } catch (Exception e) {
                        
                        // This is just a hail mary in case something unexpected happens like an NPE and isn't
                        // caught by the action listener.
                        Environment.logError ("Something wrong on action",
                                              e);
                        
                    }
                    
                }
                
            });
                        
        }
        
        buttons.put (Environment.getButtonLabel (cancelButtonLabel,
                                                 Constants.CANCEL_BUTTON_LABEL_ID),
                     new ActionAdapter ()
        {
           
            public void actionPerformed (ActionEvent ev)
            {
                    
                if (onCancel != null)
                {
                    
                    try
                    {
                    
                        onCancel.actionPerformed (ev);
                        
                    } catch (Exception e) {
                                                        
                        // This is just a hail mary in case something unexpected happens like an NPE and isn't
                        // caught by the action listener.
                        Environment.logError ("Something wrong on action",
                                              e);
                                    
                    }
                    
                }
                                        
            }
            
        });
                    
        return UIUtils.createQuestionPopup (popup,
                                            title,
                                            icon,
                                            message,
                                            buttons,
                                            null,
                                            showAt);
        
    }
    
    public static MessageWindow createQuestionPopup (PopupWindow           popup,
                                                     String                title,
                                                     String                icon,
                                                     String                message,
                                                     final Map<String, ActionListener> buttons,
                                                     final ActionListener              onClose,
                                                     Point                 showAt)
    {
                
        final MessageWindow mw = new MessageWindow ((popup != null ? popup.getProjectViewer () : null),
                                                    title,
                                                    message)
        {
            
            public JButton[] getButtons ()
            {

                JButton[] buts = new JButton[buttons.size ()];
                
                int i = 0; 
                
                // Too much type information required to do entrySet...
                // We will never do much iterating here so simpler and clearer to just do it the "hard way".
                Iterator<String> iter = buttons.keySet ().iterator ();
                
                while (iter.hasNext ())
                {
                    
                    String label = iter.next ();
                    
                    ActionListener a = buttons.get (label);
                    
                    JButton b = UIUtils.createButton (label,
                                                      null);
                
                    b.addActionListener (this.getCloseAction ());
        
                    if (a != null)
                    {
                        
                        b.addActionListener (a);
                        
                    }
                        
                    buts[i++] = b;
                                
                }
                            
                return buts;
                
            }
            
        };

        mw.init ();

        // If show at is null then show the message window at the center of the popup.
        if (showAt == null)
        {
            
            Rectangle pbounds = popup.getBounds ();
    
            Dimension size = mw.getPreferredSize ();
            
            int x = ((pbounds.width - size.width) / 2) + pbounds.x;
            int y = ((pbounds.height - size.height) / 2) + pbounds.y;
    
            // Move the window
            showAt = new Point (x,
                                y);

        }
        
        mw.setShowAt (showAt);
        
        // Add the on close
        if (onClose != null)
        {

            mw.addWindowListener (new WindowAdapter ()
            {

                public void windowClosing (WindowEvent ev)
                {

                    onClose.actionPerformed (new ActionEvent (mw, 0, "closing"));

                }

            });
                        
        }
        
        return mw;
        
    }
    
    public static QPopup createQuestionPopup (AbstractProjectViewer viewer,
                                              String                title,
                                              String                icon,
                                              String                message,
                                              String                confirmButtonLabel,
                                              String                cancelButtonLabel,
                                              final ActionListener  onConfirm,
                                              final ActionListener  onCancel,
                                              Point                 showAt)
    {
                        
        Map<String, ActionListener> buttons = new LinkedHashMap ();
        
        if (onConfirm != null)
        {
        
            buttons.put (Environment.getButtonLabel (confirmButtonLabel,
                                                     Constants.CONFIRM_BUTTON_LABEL_ID),
                         onConfirm);
            
        }
        
        buttons.put (Environment.getButtonLabel (cancelButtonLabel,
                                                 Constants.CANCEL_BUTTON_LABEL_ID),
                     onCancel);
        
        return UIUtils.createQuestionPopup (viewer,
                                            title,
                                            icon,
                                            message,
                                            buttons,
                                            onCancel,
                                            showAt);
        
    }

    public static QPopup createQuestionPopup (AbstractProjectViewer       viewer,
                                              String                      title,
                                              String                      icon,
                                              String                      message,
                                              Map<String, ActionListener> buttons,
                                              ActionListener              onClose,
                                              Point                       showAt)
    {
                
        final QPopup qp = UIUtils.createClosablePopup (title,
                                                       Environment.getIcon (icon,
                                                                            Constants.ICON_POPUP),
                                                       onClose);
        
        Box content = new Box (BoxLayout.Y_AXIS);

        JComponent mess = UIUtils.createHelpTextPane (Environment.replaceObjectNames (message),
                                                      viewer);
        mess.setBorder (null);
        mess.setSize (new Dimension (DEFAULT_POPUP_WIDTH - 20,
                                     500));
        
        content.add (mess);
        content.add (Box.createVerticalStrut (10));
                            
        JButton[] buts = new JButton[buttons.size ()];
        
        int i = 0; 
        
        // Too much type information required to do entrySet...
        // We will never do much iterating here so simpler and clearer to just do it the "hard way".
        Iterator<String> iter = buttons.keySet ().iterator ();
        
        while (iter.hasNext ())
        {
            
            String label = iter.next ();
            
            ActionListener a = buttons.get (label);
            
            JButton b = UIUtils.createButton (label,
                                              null);
        
            b.addActionListener (qp.getCloseAction ());

            if (a != null)
            {
                
                b.addActionListener (a);
                
            }
                
            buts[i++] = b;
                        
        }
            
        JComponent bs = UIUtils.createButtonBar2 (buts,
                                                  Component.LEFT_ALIGNMENT); //ButtonBarFactory.buildLeftAlignedBar (buts);
        bs.setAlignmentX (Component.LEFT_ALIGNMENT);
        content.add (bs);
        content.setBorder (new EmptyBorder (10, 10, 10, 10));
        qp.setContent (content);

        content.setPreferredSize (new Dimension (UIUtils.DEFAULT_POPUP_WIDTH,
                                                 content.getPreferredSize ().height));

        if (showAt == null)
        {
        
            showAt = UIUtils.getCenterShowPosition (viewer,
                                                    qp);
            
        }
        
        viewer.showPopupAt (qp,
                            showAt);
        
        qp.setDraggable (viewer);
        
        return qp;
        
    }

    public static QPopup createTextInputPopup (final AbstractProjectViewer viewer,
                                               String                title,
                                               String                icon,
                                               String                message,
                                               String                confirmButtonLabel,
                                               String                initValue,
                                               final ValueValidator  validator,
                                               final ActionListener  onConfirm,
                                               final ActionListener  onCancel,
                                               Point                 showAt)
    {

        final QPopup qp = UIUtils.createPopup (Environment.replaceObjectNames (title),
                                               icon,
                                               null,
                                               true,
                                               onCancel);
/*
        JButton bt = new JButton (Environment.getIcon (Constants.CANCEL_ICON_NAME,
                                                       Constants.ICON_MENU));
        UIUtils.setAsButton (bt);
        bt.setOpaque (false);

        bt.addActionListener (new ActionAdapter ()
        {
           
            public void actionPerformed (ActionEvent ev)
            {
                
                if (onCancel != null)
                {
                    
                    onCancel.actionPerformed (ev);
                    
                }
                
                qp.removeFromParent ();
                
            }
            
        });
        */

        /*
        JToolBar tb = new JToolBar ();
        tb.setOpaque (false);
        tb.setFloatable (false);
        tb.setBackground (UIUtils.getComponentColor ());

        tb.add (bt);

        qp.getHeader ().setControls (tb);
        */
        final Box content = new Box (BoxLayout.Y_AXIS);

        JComponent mess = UIUtils.createHelpTextPane (message,
                                                      viewer);
        mess.setBorder (null);
        mess.setSize (new Dimension (DEFAULT_POPUP_WIDTH - 20,
                                     500));

        content.add (mess);

        content.add (Box.createVerticalStrut (10));

        final JLabel error = UIUtils.createErrorLabel ("Please enter a value.");
        
        error.setVisible (false);
        error.setBorder (new EmptyBorder (0,
                                          0,
                                          5,
                                          0));
                                                
        final JTextField text = UIUtils.createTextField ();

        text.setMinimumSize (new Dimension (300,
                                            text.getPreferredSize ().height));
        text.setPreferredSize (new Dimension (300,
                                              text.getPreferredSize ().height));
        text.setMaximumSize (new Dimension (Short.MAX_VALUE,
                                            text.getPreferredSize ().height));
        text.setAlignmentX (Component.LEFT_ALIGNMENT);
        
        if (initValue != null)
        {
            
            text.setText (initValue);
            
        }
        
        error.setAlignmentX (Component.LEFT_ALIGNMENT);        
        
        content.add (error);
        content.add (text);
        
        content.add (Box.createVerticalStrut (10));
        
        JButton confirm = null;
        JButton cancel = UIUtils.createButton ("Cancel",
                                               null);
        
        if (onConfirm != null)
        {
            
            confirm = UIUtils.createButton (confirmButtonLabel,
                                            null);

            ActionListener confirmAction = new ActionAdapter ()
            {
                
                public void actionPerformed (ActionEvent ev)
                {

                    if (validator != null)
                    {
                        
                        String mess = validator.isValid (text.getText ().trim ());
                        
                        if (mess != null)
                        {

                            error.setText (Environment.replaceObjectNames (mess));
                                                        
                            error.setVisible (true);
                                                
                            // Got to be an easier way of doing this.
                            content.setPreferredSize (null);

                            content.setPreferredSize (new Dimension (UIUtils.DEFAULT_POPUP_WIDTH,
                                                                     content.getPreferredSize ().height));

                            viewer.showPopupAt (qp,
                                                qp.getLocation ());
                            
                            return;
                            
                        }
                                                
                    }
                                        
                    onConfirm.actionPerformed (new ActionEvent (text,
                                                                0,
                                                                text.getText ().trim ()));
                    
                    qp.removeFromParent ();
                    
                }
                
            };

            UIUtils.addDoActionOnReturnPressed (text,
                                                confirmAction);
            confirm.addActionListener (confirmAction);
                        
        }
        
        cancel.addActionListener (new ActionAdapter ()
        {
           
            public void actionPerformed (ActionEvent ev)
            {
                
                if (onCancel != null)
                {
                    
                    onCancel.actionPerformed (ev);
                                    
                }
                
                qp.removeFromParent ();
                
            }
            
        });
                    
        JButton[] buts = null;
        
        if (confirm != null)
        {
            
            buts = new JButton[] { confirm, cancel };
            
        } else {
            
            buts = new JButton[] { cancel };
            
        }

        JComponent buttons = UIUtils.createButtonBar2 (buts,
                                                       Component.LEFT_ALIGNMENT); //ButtonBarFactory.buildLeftAlignedBar (buts);
        buttons.setAlignmentX (Component.LEFT_ALIGNMENT);
        content.add (buttons);
        content.setBorder (new EmptyBorder (10, 10, 10, 10));
        qp.setContent (content);
                        
        content.setPreferredSize (new Dimension (UIUtils.DEFAULT_POPUP_WIDTH,
                                                 content.getPreferredSize ().height));

        if (showAt == null)
        {
        
            showAt = UIUtils.getCenterShowPosition (viewer,
                                                    qp);
            
        }

        qp.setDraggable (viewer);
                                            
        viewer.showPopupAt (qp,
                            showAt);

        if (initValue != null)
        {
            
            text.selectAll ();
                
        } 

        text.grabFocus ();
        
        return qp;
        
    }

    public static Point getCenterShowPosition (Component parent,
                                               Component show)
    {
        
        Dimension pd = (parent.isShowing () ? parent.getSize () : parent.getPreferredSize ());
        Dimension sd = (show.isShowing () ? show.getSize () : show.getPreferredSize ());
        
        return new Point ((pd.width - sd.width) / 2,
                          (pd.height - sd.height) / 2);
        
    }

    public static void doLater (ActionListener l)
    {
        
        UIUtils.doLater (l,
                         null,
                         null);
        
    }

    public static void doLater (final ActionListener l,
                                final Component      c,
                                final String         showOnError)
    {
        
        SwingUtilities.invokeLater (new Runnable ()
        {
                                 
            public void run ()
            {
            
               try
               {
                   
                   l.actionPerformed (new ActionEvent ((c != null ? c : this),
                                                       0,
                                                       "do"));
   
               } catch (Exception e) {
                   
                   if (showOnError != null)
                   {
                   
                        Environment.logError (showOnError,
                                              e);
                        
                        UIUtils.showErrorMessage (c,
                                                  showOnError);

                   } else {
                    
                        Environment.logError ("Unable to perform action",
                                              e);
                        
                        UIUtils.showErrorMessage (c,
                                                  "Unable to perform action");                        
                    
                   }
                   
               }
               
            }
            
        });
                
        
    }
    
}