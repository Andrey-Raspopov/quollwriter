package com.quollwriter.ui;

import java.awt.*;
import java.io.*;
import java.awt.datatransfer.*;
import java.awt.event.*;
import java.util.*;
import java.util.concurrent.*;
import java.text.*;
import java.net.*;
import java.nio.file.*;

import javax.swing.*;
import javax.swing.border.*;
import javax.swing.text.*;
import javax.swing.event.*;
import javax.swing.filechooser.*;
import javax.swing.plaf.LayerUI;
import javax.swing.tree.*;

import com.gentlyweb.properties.*;
import com.gentlyweb.utils.*;

import org.josql.*;

import com.jgoodies.forms.builder.*;
import com.jgoodies.forms.factories.*;
import com.jgoodies.forms.layout.*;

import com.quollwriter.*;
import com.quollwriter.events.*;
import com.quollwriter.data.*;
import com.quollwriter.ui.events.*;
import com.quollwriter.data.comparators.*;
import com.quollwriter.data.editors.*;
import com.quollwriter.editors.*;
import com.quollwriter.ui.components.*;
import com.quollwriter.ui.panels.*;
import com.quollwriter.text.*;
import com.quollwriter.db.*;
import com.quollwriter.ui.sidebars.*;
import com.quollwriter.editors.*;
import com.quollwriter.editors.ui.*;
import com.quollwriter.editors.ui.sidebars.*;
import com.quollwriter.achievements.ui.*;
import com.quollwriter.ui.charts.*;
import com.quollwriter.ui.actionHandlers.*;
import com.quollwriter.ui.forms.*;
import com.quollwriter.ui.components.ScrollableBox;
import com.quollwriter.ui.components.SpellChecker;
import com.quollwriter.uistrings.*;

import static com.quollwriter.LanguageStrings.*;
import static com.quollwriter.Environment.getUIString;

public class WebsiteLanguageStringsEditor extends AbstractLanguageStringsEditor<WebsiteLanguageStrings, WebsiteLanguageStrings>
{

    public WebsiteLanguageStringsEditor (WebsiteLanguageStrings userStrings)
			                      throws Exception
    {

        super (userStrings);

    }

    @Override
    public void onForwardLabelClicked ()
                                throws Exception
    {

        if (this.nodeFilter != null)
        {

            this.showAllStrings ();

            this.showForwardLabel (String.format ("Click to show what's changed/new between version <b>%s</b> and <b></b>.",
                                                  this.userStrings.getStringsVersion ()));

        } else {

            this.limitViewToPreviousVersionDiff ();

        }

    }

    @Override
    public void tryOut ()
    {

    }

    @Override
    public void init ()
               throws Exception
    {

        final WebsiteLanguageStringsEditor _this = this;

        super.init ();

        this.updateTitle ();

        // Check to see if a new version of the default strings is available.
        Environment.schedule (new Runnable ()
        {

            @Override
            public void run ()
            {

                WebsiteLanguageStrings enStrs = null;

                try
                {

                    enStrs = Environment.getWebsiteLanguageStringsFromServer ();

                } catch (Exception e) {

                    Environment.logError ("Unable to get English Website UI language strings",
                                          e);

                    UIUtils.showErrorMessage (_this,
                                              "Unable to get English Website UI language strings.");

                    return;

                }

                 if (enStrs.getStringsVersion () == _this.userStrings.getDerivedFrom ().getStringsVersion ())
                 {

                     // Server still has same version.
                     return;

                 }

                 try
                 {

                     // Save the new strings away.
                     Environment.saveWebsiteLanguageStrings (enStrs);

                 } catch (Exception e) {

                     Environment.logError ("Unable to save new English Website UI language strings",
                                           e);

                     UIUtils.showErrorMessage (_this,
                                               "Unable to save new English Website UI language strings.");

                     return;

                 }

                 final WebsiteLanguageStrings _enStrs = enStrs;

                 Box content = new Box (BoxLayout.Y_AXIS);

                 content.add (UIUtils.createHelpTextPane (String.format ("A new version of the website language strings is available.<br />You can view the changes and submit an update to your strings."),
                                                          _this));

                 content.add (Box.createVerticalStrut (5));

                 JButton b = UIUtils.createButton ("View the changes");

                 content.add (b);

                 // Add a notification.
                 final Notification n = _this.addNotification (content,
                                                               Constants.INFO_ICON_NAME,
                                                               -1);

                  b.addActionListener (new ActionListener ()
                  {

                       @Override
                       public void actionPerformed (ActionEvent ev)
                       {

                           WebsiteLanguageStrings uls = null;

                           try
                           {

                               uls = Environment.getWebsiteLanguageStrings (_this.userStrings.getStringsVersion (),
                                                                            _this.userStrings.getId ());

                           } catch (Exception e) {

                               Environment.logError ("Unable to get user strings: " + _this.userStrings.getId (),
                                                     e);

                               UIUtils.showErrorMessage (null,
                                                         getUIString (uilanguage,edit,actionerror));

                               return;

                           }

                           if (uls != null)
                           {

                               // Open these instead.
                               WebsiteLanguageStringsEditor lse = Environment.editWebsiteLanguageStrings (uls);

                               try
                               {

                                   lse.limitViewToPreviousVersionDiff ();

                               } catch (Exception e) {

                                   Environment.logError ("Unable to update view",
                                                         e);

                                   UIUtils.showErrorMessage (_this,
                                                             "Unable to update view");

                                   return;

                               }

                               _this.removeNotification (n);

                               return;

                           }

                           _this.showChanges (_enStrs);

                           _this.removeNotification (n);

                       }

                  });

              }

        },
        5,
        -1);

    }

    public void limitViewToPreviousVersionDiff ()
                                         throws Exception
    {

        try
        {

            this.save ();

        } catch (Exception e) {

            Environment.logError ("Unable to save",
                                  e);

            UIUtils.showErrorMessage (this,
                                      "Unable to update view.");

            return;

        }

        final WebsiteLanguageStrings basels = this.baseStrings;

        final WebsiteLanguageStrings prevbasels = Environment.getWebsiteLanguageStrings (1,
                                                                                         WebsiteLanguageStrings.ENGLISH_ID);

        if (prevbasels == null)
        {

            // No strings.
            return;

        }

        this.setNodeFilter (new Filter<Node> ()
        {

            @Override
            public boolean accept (Node n)
            {

                Node pn = prevbasels.getNode (n.getId ());

                // Does the node exist in the current base strings but not the previous?
                if (pn == null)
                {

                    // This is a new node.
                    return true;

                }

                // It exists, but has it changed?
                if ((n instanceof Value)
                    &&
                    (!(pn instanceof Value))
                   )
                {

                    // Node type changed.
                    return true;

                }

                if ((pn instanceof Value)
                    &&
                    (!(n instanceof Value))
                   )
                {

                    // Node type changed.
                    return true;

                }

                if ((pn instanceof TextValue)
                    &&
                    (n instanceof TextValue)
                   )
                {

                    TextValue pnv = (TextValue) pn;
                    TextValue nv = (TextValue) n;

                    // Value changed?
                    if (pnv.getRawText ().equals (nv.getRawText ()))
                    {

                        return false;

                    }

                }

                return true;

            }

        });

        this.showForwardLabel (String.format ("Click to show all the strings for version <b>%s</b>.",
                                              this.userStrings.getStringsVersion ()));
/*
      this.showForwardLabel (String.format ("Click to show what's changed/new between version <b>%s</b> and <b>%s</b>.",
                                            Environment.getQuollWriterVersion ().toString (),
                                            this.userStrings.getQuollWriterVersion ().toString ()));
*/

    }

    public void showChanges (WebsiteLanguageStrings newls)
    {

        try
        {

            Environment.saveWebsiteLanguageStrings (newls);

            Environment.saveWebsiteLanguageStrings (this.userStrings);

            WebsiteLanguageStrings uls = Environment.getWebsiteLanguageStrings (this.userStrings.getStringsVersion (),
                                                                                this.userStrings.getId ());

            // Get a diff of the default to this new.
            WebsiteLanguageStringsEditor lse = Environment.editWebsiteLanguageStrings (uls);

            lse.limitViewToPreviousVersionDiff ();

        } catch (Exception e) {

            Environment.logError ("Unable to show strings editor for: " +
                                  newls,
                                  e);

            UIUtils.showErrorMessage (this,
                                      "Unable to show strings.");

        }

    }

	@Override
    public Set<String> getTitleHeaderControlIds ()
	{

		Set<String> ids = new LinkedHashSet ();

        ids.add (SUBMIT_HEADER_CONTROL_ID);
        ids.add (FIND_HEADER_CONTROL_ID);
        //ids.add (USE_HEADER_CONTROL_ID);
        ids.add (REPORT_BUG_HEADER_CONTROL_ID);
        ids.add (HELP_HEADER_CONTROL_ID);
        ids.add (SETTINGS_HEADER_CONTROL_ID);

		return ids;

	}

	@Override
    public JComponent getTitleHeaderControl (String id)
	{

		if (id == null)
		{

			return null;

		}

        java.util.List<String> prefix = Arrays.asList (allprojects,headercontrols,items);

		final WebsiteLanguageStringsEditor _this = this;

		JComponent c = null;

        if (id.equals (HELP_HEADER_CONTROL_ID))
        {

            c = UIUtils.createButton (Constants.HELP_ICON_NAME,
                                      Constants.ICON_TITLE_ACTION,
                                      "Click to view the help about editing your strings",
                                      new ActionListener ()
                                      {

                                        @Override
                                        public void actionPerformed (ActionEvent ev)
                                        {

                                            UIUtils.openURL (_this,
                                                             "help://uilanguages/overview");

                                        }

                                      });

        }

		if (c != null)
		{

			return c;

		}

		return super.getTitleHeaderControl (id);

	}

    @Override
    public void submit ()
    {

        final WebsiteLanguageStringsEditor _this = this;

        try
        {

            this.save ();

        } catch (Exception e) {

            Environment.logError ("Unable to save: " + this.userStrings,
                                  e);

            UIUtils.showErrorMessage (this,
                                      "Unable to save strings.");

            return;

        }

        Set<Value> vals = this.userStrings.getAllValues ();

        if (vals.size () == 0)
        {

            UIUtils.showMessage ((PopupsSupported) this,
                                 "No strings provided",
                                 "Sorry, you must provide at least 1 string.");

            return;

        }

        int c = 0;

        for (Value uv : vals)
        {

            Value bv = this.baseStrings.getTextValue (uv.getId ());

            if (bv == null)
            {

                // The string is present in the user strings but not the base!
                Environment.logError ("Found string: " + uv.getId () + " present in user strings but not base.");

                continue;

            }

            c += bv.getErrors (this).size ();

        }

        if (c > 0)
        {

            UIUtils.showMessage ((PopupsSupported) this,
                                 "Errors found in strings",
                                 String.format ("Sorry, there are <b>%s</b> errors that must be corrected before you can submit the strings.",
                                                Environment.formatNumber (c)));

            return;

        }

        final String popupName = "submit";
        QPopup popup = this.getNamedPopup (popupName);

        if (popup == null)
        {

            popup = UIUtils.createClosablePopup ("Submit your strings",
                                                 Environment.getIcon (Constants.UP_ICON_NAME,
                                                                      Constants.ICON_POPUP),
                                                 null);

            final QPopup qp = popup;

            Box content = new Box (BoxLayout.Y_AXIS);

            JTextPane help = UIUtils.createHelpTextPane ("Complete the form below to submit your strings.  All the values are required.",
                                                         this);

            help.setBorder (null);

            content.add (help);
            content.add (Box.createVerticalStrut (10));

            final JLabel error = UIUtils.createErrorLabel ("");
            error.setVisible (false);
            error.setBorder (UIUtils.createPadding (0, 5, 5, 5));

            content.add (error);

            Set<FormItem> items = new LinkedHashSet ();

            final TextFormItem nativelang = new TextFormItem ("Native Name (i.e. Espa\u00F1ol, Fran\u00E7ais, Deutsch)",
                                                             this.userStrings.getNativeName ());

            items.add (nativelang);

            final TextFormItem lang = new TextFormItem ("English Name (i.e. Spanish, French, German)",
                                                         this.userStrings.getLanguageName ());

            items.add (lang);

            final TextFormItem email = new TextFormItem ("Contact Email",
                                                         this.userStrings.getEmail ());

            items.add (email);

            final JLabel tc = UIUtils.createClickableLabel ("View the Terms and Conditions for creating a translation",
                                                            Environment.getIcon (Constants.INFO_ICON_NAME,
                                                                                 Constants.ICON_CLICKABLE_LABEL),
                                                            Environment.getQuollWriterHelpLink ("uilanguages/terms-and-conditions",
                                                                                                null));

            final CheckboxFormItem tandc = new CheckboxFormItem (null, "I have read and agree to the Terms and Conditions");

            if (this.userStrings.getStringsVersion () == 0)
            {

                items.add (new AnyFormItem (null, tc));
                items.add (tandc);

            }

            ActionListener saveAction = new ActionListener ()
            {

                @Override
                public void actionPerformed (ActionEvent ev)
                {

                    String em = email.getValue ();

                    if (em == null)
                    {

                        error.setText ("Please enter a valid email.");
                        error.setVisible (true);
                        qp.resize ();
                        qp.resize ();

                        return;

                    }

                    String l = lang.getValue ();

                    if (l == null)
                    {

                        error.setText ("Please enter the English Language name.");
                        error.setVisible (true);
                        qp.resize ();

                        return;

                    }

                    String nl = nativelang.getValue ();

                    if (nl == null)
                    {

                        error.setText ("Please enter the Name Language name.");
                        error.setVisible (true);
                        qp.resize ();

                        return;

                    }

                    if (_this.userStrings.getAllValues ().size () == 0)
                    {

                        error.setText ("No strings provided.  Please provide at least 1 string for your translation.");
                        error.setVisible (true);
                        qp.resize ();

                        return;

                    }

                    if ((_this.userStrings.getStringsVersion () == 0)
                        &&
                        (!tandc.isSelected ())
                       )
                    {

                        error.setText ("To submit your strings you must agree to the Terms & Conditions, and please give them a quick read ;)");
                        error.setVisible (true);
                        qp.resize ();

                        return;

                    }

                    _this.userStrings.setEmail (em);
                    _this.userStrings.setLanguageName (l);
                    _this.userStrings.setNativeName (nl);

                    _this.updateTitle ();

                    try
                    {

                        _this.save ();

                    } catch (Exception e) {

                        Environment.logError ("Unable to save strings: " + _this.userStrings,
                                              e);

                        UIUtils.showErrorMessage (_this,
                                                  "Unable to save strings.");

                        return;

                    }

                    // Get the file, then send.
                    String t = null;

                    try
                    {

                        t = JSONEncoder.encode (_this.userStrings.getAsJSON ());

                    } catch (Exception e)
                    {

                        Environment.logError ("Unable to upload strings: " + _this.userStrings,
                                              e);

                        UIUtils.showErrorMessage (_this,
                                                  "Unable to upload strings.");

                        return;

                    }

                    Map<String, Object> entries = new HashMap<> ();
                    entries.put ("/strings", t);

                    for (ImageValue iv : _this.userStrings.getAllImageValues ())
                    {

                        ImageValue bv = _this.baseStrings.getImageValue (iv.getId ());

                        entries.put (BaseStrings.toId (bv.getId ()),
                                     iv.getImageFile ());

                    }

                    Path _outputFile = null;

                    try
                    {

                        _outputFile = Files.createTempFile ("qw-upload",
                                                            ".zip");

                    } catch (Exception e) {

                        Environment.logError ("Unable to create output file",
                                              e);

                        UIUtils.showErrorMessage (_this,
                                                  "Unable to upload strings.");

                        return;

                    }

                    final Path outputFile = _outputFile;

                    outputFile.toFile ().deleteOnExit ();

                    try
                    {

                        Utils.createZipFile (outputFile,
                                             entries);

                    } catch (Exception e) {

                        Environment.logError ("Unable to create zip file: " + outputFile,
                                              e);

                        UIUtils.showErrorMessage (_this,
                                                  "Unable to upload strings.");

                        return;

                    }

                    Map<String, String> headers = new HashMap<> ();

                    String submitterid = UserProperties.get (Constants.WEBSITE_LANGUAGE_STRINGS_SUBMITTER_ID_PROPERTY_NAME);

                    if (submitterid != null)
                    {

                        headers.put (Constants.WEBSITE_LANGUAGE_STRINGS_SUBMITTER_ID_HEADER_NAME,
                                     submitterid);

                    }

                    URL u = null;

                    try
                    {

                        u = new URL (Environment.getQuollWriterWebsite () + Environment.getProperty (Constants.SUBMIT_WEBSITE_LANGUAGE_STRINGS_URL_PROPERTY_NAME));

                        Utils.postToURL (u,
                                         headers,
                                         outputFile,
                                         // On success
                                         new ActionListener ()
                                         {

                                             @Override
                                             public void actionPerformed (ActionEvent ev)
                                             {
Environment.out.println ("HERE");
                                                 try
                                                 {

                                                     Files.deleteIfExists (outputFile);

                                                 } catch (Exception e) {

                                                     Environment.logError ("Unable to delete temp file: " + outputFile,
                                                                           e);

                                                 }

                                                 Map m = (Map) JSONDecoder.decode ((String) ev.getSource ());
Environment.out.println ("GOT: " + m);
                                                 String res = (String) m.get ("result");

                                                 String sid = (String) m.get ("submitterid");

                                                 UserProperties.set (Constants.WEBSITE_LANGUAGE_STRINGS_SUBMITTER_ID_PROPERTY_NAME,
                                                                     sid);

                                                 //_this.userStrings.setSubmitterId (sid);
                                                 _this.userStrings.setStringsVersion (((Number) m.get ("version")).intValue ());

                                                 try
                                                 {

                                                     _this.saveToFile ();

                                                 } catch (Exception e) {

                                                     Environment.logError ("Unable to save strings file: " +
                                                                           _this.userStrings,
                                                                           e);

                                                     UIUtils.showErrorMessage (_this,
                                                                               "Your strings have been submitted to Quoll Writer support for review.  However the associated local file, where the strings are kept on your machine, could not be updated.");

                                                     return;

                                                 }

                                                 if (_this.userStrings.getStringsVersion () == 1)
                                                 {

                                                     UIUtils.showMessage ((PopupsSupported) _this,
                                                                          "Strings submitted",
                                                                          String.format ("Your strings have been submitted to Quoll Writer support for review.<br /><br />A confirmation email has been sent to <b>%s</b>.  Please click on the link in that email to confirm your email address.<br /><br />Thank you for taking the time and the effort to create the strings, it is much appreciated!",
                                                                                         _this.userStrings.getEmail ()));

                                                 } else {

                                                     UIUtils.showMessage ((PopupsSupported) _this,
                                                                          "Strings submitted",
                                                                          String.format ("Thank you!  Your strings have been updated to version <b>%s</b> and will be made available to Quoll Writer users.<br /><br />Thank you for taking the time and effort to update the strings, it is much appreciated!",
                                                                                         Environment.formatNumber (_this.userStrings.getStringsVersion ())));

                                                 }

                                                 qp.resize ();
                                                 qp.removeFromParent ();

                                             }

                                         },
                                         // On error
                                         new ActionListener ()
                                         {

                                             @Override
                                             public void actionPerformed (ActionEvent ev)
                                             {

                                                 try
                                                 {

                                                     Files.deleteIfExists (outputFile);

                                                 } catch (Exception e) {

                                                     Environment.logError ("Unable to delete temp file: " + outputFile,
                                                                           e);

                                                 }

                                                 Map m = (Map) JSONDecoder.decode ((String) ev.getSource ());

                                                 String res = (String) m.get ("reason");

                                                 // Get the errors.
                                                 UIUtils.showErrorMessage (_this,
                                                                           "Unable to submit the strings, reason:<ul class='error'><li>" + res + "</li></ul>");

                                             }

                                         },
                                         new ActionListener ()
                                         {

                                             @Override
                                             public void actionPerformed (ActionEvent ev)
                                             {

                                                 Map m = (Map) JSONDecoder.decode ((String) ev.getSource ());

                                                 String res = (String) m.get ("reason");

                                                 // Get the errors.
                                                 UIUtils.showErrorMessage (_this,
                                                                           "Unable to submit the strings, reason:<ul class='error'><li>" + res + "</li></ul>");

                                             }

                                         });

                     } catch (Exception e) {

                         Environment.logError ("Unable to post to the url.",
                                               e);

                         UIUtils.showErrorMessage (_this,
                                                   "Unable to upload strings.");

                         return;

                     }

                }

            };

            UIUtils.addDoActionOnReturnPressed (lang.getTextField (),
                                                saveAction);
            UIUtils.addDoActionOnReturnPressed (nativelang.getTextField (),
                                                saveAction);
            UIUtils.addDoActionOnReturnPressed (email.getTextField (),
                                                saveAction);

            JButton save = UIUtils.createButton ("Submit",
                                                 saveAction);
            JButton cancel = UIUtils.createButton ("Cancel",
                                                   new ActionListener ()
            {

                @Override
                public void actionPerformed (ActionEvent ev)
                {

                    qp.removeFromParent ();
                    _this.removeNamedPopup (popupName);

                }

            });

            Set<JButton> buttons = new LinkedHashSet ();
            buttons.add (save);
            buttons.add (cancel);

            Form f = new Form (Form.Layout.stacked,
                               items,
                               buttons);

            content.add (f);

            content.setSize (new Dimension (UIUtils.getPopupWidth () - 20,
                             content.getPreferredSize ().height));
            content.setBorder (UIUtils.createPadding (10, 10, 10, 10));

            popup.setContent (content);

            popup.setDraggable (this);

            popup.setPopupName (popupName);

            this.addNamedPopup (popupName,
                                popup);

            //popup.resize ();
            this.showPopupAt (popup,
                              UIUtils.getCenterShowPosition (this,
                                                             popup),
                              false);

            UIUtils.doLater (new ActionListener ()
            {

                @Override
                public void actionPerformed (ActionEvent ev)
                {

                    //desc.grabFocus ();
                    qp.resize ();

                }

            });

        } else {

            popup.setVisible (true);
            popup.resize ();

        }

    }

	@Override
    public String getViewerIcon ()
    {

        return Constants.EDIT_ICON_NAME;

    }

	@Override
    public void fillSettingsPopup (JPopupMenu popup)
	{

        //java.util.List<String> prefix = Arrays.asList (allprojects,settingsmenu,items);

		final WebsiteLanguageStringsEditor _this = this;

        popup.add (this.createMenuItem ("Submit your strings",
                                        Constants.UP_ICON_NAME,
                                        new ActionAdapter ()
                                        {

                                                public void actionPerformed (ActionEvent ev)
                                                {

                                                    _this.submit ();

                                                }

                                            }));

        popup.add (this.createMenuItem ("Create a new translation",
                                        Constants.NEW_ICON_NAME,
										new ActionListener ()
											 {

												@Override
												public void actionPerformed (ActionEvent ev)
												{

                                                    UIUtils.showAddNewWebsiteLanguageStringsPopup (_this);

												}

											 }));

        popup.add (this.createMenuItem ("Edit a translation",
                                        Constants.EDIT_ICON_NAME,
 			    						new ActionListener ()
 										{

											@Override
											public void actionPerformed (ActionEvent ev)
											{

                                                UIUtils.showEditWebsiteLanguageStringsSelectorPopup (_this);

											}

										}));
/*
        if (this.baseStrings.getQuollWriterVersion ().equals (Environment.getQuollWriterVersion ()))
        {

            popup.add (this.createMenuItem ("Try out your strings",
                                            Constants.PLAY_ICON_NAME,
    										new ActionListener ()
    											 {

    												@Override
    												public void actionPerformed (ActionEvent ev)
    												{

                                                        //_this.tryOut ();

    												}

    											 }));

        }
*/
        popup.add (this.createMenuItem ("Delete",
                                     Constants.DELETE_ICON_NAME,
										new ActionListener ()
											 {

												@Override
												public void actionPerformed (ActionEvent ev)
												{

                                                    try
                                                    {

                                                        _this.delete ();

                                                    } catch (Exception e) {

                                                        Environment.logError ("Unable to delete strings: " + _this.userStrings.getId (),
                                                                              e);

                                                        UIUtils.showErrorMessage (_this,
                                                                                  "Unable to delete strings.");

                                                    }

												}

											 }));

         popup.add (this.createMenuItem ("Close",
                                      Constants.CLOSE_ICON_NAME,
 										new ActionListener ()
 											 {

 												@Override
 												public void actionPerformed (ActionEvent ev)
 												{

                                                  _this.close (true,
                                                               null);

 												}

 											 }));

        popup.addSeparator ();

        popup.add (this.createMenuItem ("Open Project",
                                        Constants.UP_ICON_NAME,
                                        new ActionAdapter ()
                                        {

                                                public void actionPerformed (ActionEvent ev)
                                                {

                                                    Environment.showLanding ();

                                                }

                                            }));


	}

    public WebsiteLanguageStrings getUserLanguageStrings ()
    {

        return this.userStrings;

    }

    @Override
    public void delete ()
    throws Exception
    {

        final WebsiteLanguageStringsEditor _this = this;

        QPopup qp = UIUtils.createPopup ("Delete the strings",
                                         Constants.DELETE_ICON_NAME,
                                         null,
                                         true,
                                         null);

        final Box content = new Box (BoxLayout.Y_AXIS);

        JComponent mess = UIUtils.createHelpTextPane ("Please confirm you wish to delete your strings.  Enter <b>Yes</b> in the box below to confirm deletion.<br /><br />To delete <b>all</b> versions of the strings please check the box below.<br /><br /><span class='warning'>Warning!  This is an irreverisble operation and cannot be undone.  This will make your strings unavailable to {QW} users but will not remove it from anyone who has already downloaded the strings.",
                                                      this);
        mess.setBorder (null);
        mess.setSize (new Dimension (UIUtils.getPopupWidth () - 20,
                                     mess.getPreferredSize ().height));

        content.add (mess);

        content.add (Box.createVerticalStrut (10));

        final JCheckBox delAll = UIUtils.createCheckBox ("Delete all versions");

        content.add (delAll);

        content.add (Box.createVerticalStrut (10));

        final JLabel error = UIUtils.createErrorLabel ("Please enter the word Yes.");
        //"Please enter a value.");

        error.setVisible (false);
        error.setBorder (UIUtils.createPadding (0, 0, 5, 0));

        final JTextField text = UIUtils.createTextField ();

        text.setMinimumSize (new Dimension (300,
                                            text.getPreferredSize ().height));
        text.setPreferredSize (new Dimension (300,
                                              text.getPreferredSize ().height));
        text.setMaximumSize (new Dimension (Short.MAX_VALUE,
                                            text.getPreferredSize ().height));
        text.setAlignmentX (Component.LEFT_ALIGNMENT);

        error.setAlignmentX (Component.LEFT_ALIGNMENT);

        content.add (error);
        content.add (text);

        content.add (Box.createVerticalStrut (10));

        JButton confirm = null;
        JButton cancel = UIUtils.createButton ("No, keep them",
                                               new ActionListener ()
        {

            @Override
            public void actionPerformed (ActionEvent ev)
            {

                qp.removeFromParent ();

            }

        });

        ActionListener confirmAction = new ActionListener ()
        {

            @Override
            public void actionPerformed (ActionEvent ev)
            {

                String mess = UIUtils.getYesValueValidator ().isValid (text.getText ().trim ());

                if (mess != null)
                {

                    // Should probably wrap this in a
                    error.setText (mess);

                    error.setVisible (true);

                    // Got to be an easier way of doing this.
                    content.setPreferredSize (null);

                    content.setPreferredSize (new Dimension (UIUtils.getPopupWidth (),
                                                             content.getPreferredSize ().height));

                    _this.showPopupAt (qp,
                                       qp.getLocation (),
                                       false);

                    return;

                }

                String submitterid = UserProperties.get (Constants.WEBSITE_LANGUAGE_STRINGS_SUBMITTER_ID_PROPERTY_NAME);

                if ((submitterid != null)
                    &&
                    // Has they been submitted?
                    (_this.userStrings.getStringsVersion () > 0)
                   )
                {

                    URL u = null;

                    try
                    {

                        String p = Environment.getProperty (Constants.DELETE_WEBSITE_LANGUAGE_STRINGS_URL_PROPERTY_NAME);

                        p = StringUtils.replaceString (p,
                                                       Constants.ID_TAG,
                                                       _this.userStrings.getId ());

                        p = StringUtils.replaceString (p,
                                                       Constants.ALL_TAG,
                                                       (delAll.isSelected () ? "true" : ""));

                        u = new URL (Environment.getQuollWriterWebsite () + p);

                    } catch (Exception e) {

                        Environment.logError ("Unable to construct the url for delete the website ui language strings.",
                                              e);

                        UIUtils.showErrorMessage (_this,
                                                  "Unable to delete strings.");

                        return;

                    }

                    Map<String, String> headers = new HashMap<> ();

                    headers.put (Constants.WEBSITE_LANGUAGE_STRINGS_SUBMITTER_ID_HEADER_NAME,
                                 submitterid);

                    Utils.postToURL (u,
                                     headers,
                                     "bogus",
                                     // On success
                                     new ActionListener ()
                                     {

                                         @Override
                                         public void actionPerformed (ActionEvent ev)
                                         {

                                             String r = (String) JSONDecoder.decode ((String) ev.getSource ());

                                             // Delete our local versions.
                                             try
                                             {

                                                 Environment.deleteWebsiteLanguageStrings (_this.userStrings,
                                                                                           delAll.isSelected ());

                                             } catch (Exception e) {

                                                 Environment.logError ("Unable to delete user strings: " + _this.userStrings,
                                                                       e);

                                                 UIUtils.showErrorMessage (_this,
                                                                           "Unable to delete the strings.");

                                                  qp.removeFromParent ();

                                                  return;

                                             }

                                             WebsiteLanguageStringsEditor.super.close (true,
                                                                                       new ActionListener ()
                                             {

                                                 @Override
                                                 public void actionPerformed (ActionEvent ev)
                                                 {

                                                     UIUtils.showMessage ((Component) null,
                                                                          "Strings deleted",
                                                                          "Your strings have been deleted.<br /><br />Thank you for the time and effort you put in to create the strings, it is much appreciated!");

                                                 }

                                             });

                                         }

                                     },
                                     // On error
                                     new ActionListener ()
                                     {

                                         @Override
                                         public void actionPerformed (ActionEvent ev)
                                         {

                                             Map m = (Map) JSONDecoder.decode ((String) ev.getSource ());

                                             String res = (String) m.get ("reason");

                                             // Get the errors.
                                             UIUtils.showErrorMessage (_this,
                                                                       "Unable to submit the strings, reason:<ul class='error'><li>" + res + "</li></ul>");

                                         }

                                     },
                                     new ActionListener ()
                                     {

                                         @Override
                                         public void actionPerformed (ActionEvent ev)
                                         {

                                             Map m = (Map) JSONDecoder.decode ((String) ev.getSource ());

                                             String res = (String) m.get ("reason");

                                             // Get the errors.
                                             UIUtils.showErrorMessage (_this,
                                                                       "Unable to delete the strings, reason:<ul class='error'><li>" + res + "</li></ul>");

                                         }

                                     });

                } else {

                    // Not been submitted.
                    // Delete our local versions.
                    try
                    {

                        Environment.deleteWebsiteLanguageStrings (_this.userStrings,
                                                                  delAll.isSelected ());

                    } catch (Exception e) {

                        Environment.logError ("Unable to delete website strings: " + _this.userStrings,
                                              e);

                        UIUtils.showErrorMessage (_this,
                                                  "Unable to delete the strings.");

                         qp.removeFromParent ();

                         return;

                    }

                    // Close without saving.
                    WebsiteLanguageStringsEditor.super.close (true,
                                                              new ActionListener ()
                    {

                        @Override
                        public void actionPerformed (ActionEvent ev)
                        {

                            UIUtils.showMessage ((Component) null,
                                                 "Strings deleted",
                                                 "Your strings have been deleted.");

                        }

                    });

                }

            }

        };

        confirm = UIUtils.createButton ("Yes, delete them",
                                        confirmAction);

        UIUtils.addDoActionOnReturnPressed (text,
                                            confirmAction);

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
        content.setBorder (UIUtils.createPadding (10, 10, 10, 10));

        content.setPreferredSize (new Dimension (UIUtils.getPopupWidth (),
                                                 content.getPreferredSize ().height));

        qp.setContent (content);
        qp.setDraggable (this);

        this.showPopupAt (qp,
                          UIUtils.getCenterShowPosition (this,
                                                         qp),
                          false);

    }

    private void saveToFile ()
                      throws Exception
    {

        Environment.saveWebsiteLanguageStrings (this.userStrings);

    }

    @Override
    public void save ()
                throws Exception
    {

        // Cycle over all the id boxes and set the values if present.
        for (LanguageStringsIdsPanel p : this.panels.values ())
        {

            p.saveValues ();

        }

        this.saveToFile ();

    }

    private void updateTitle ()
    {

        this.setViewerTitle (String.format ("Edit Website Language Strings for: %s",
                                            this.userStrings.getNativeName ()));

    }

}
