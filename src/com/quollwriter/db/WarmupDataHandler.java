package com.quollwriter.db;

import java.sql.*;

import java.util.*;

import com.quollwriter.*;

import com.quollwriter.data.*;


public class WarmupDataHandler implements DataHandler
{

    private ObjectManager objectManager = null;

    public WarmupDataHandler(ObjectManager om)
    {

        this.objectManager = om;

    }

    private Warmup getWarmup (ResultSet rs,
                              Project   p)
                       throws GeneralException
    {

        try
        {

            int ind = 1;

            long key = rs.getLong (ind++);

            Warmup w = new Warmup ();

            w.setKey (key);

            long chkey = rs.getLong (ind++);

            if (p != null)
            {

                // Get the chapter.
                Chapter c = p.getChapterByKey (chkey);

                w.setChapter (c);

            }

            String pid = rs.getString (ind++);

            if (pid != null)
            {

                w.setPrompt (Prompts.getPromptById (pid));

            }

            return w;

        } catch (Exception e)
        {

            throw new GeneralException ("Unable to load warmup",
                                        e);

        }

    }

    public List<? extends NamedObject> getObjects (NamedObject parent,
                                                   Connection  conn,
                                                   boolean     loadChildObjects)
                                            throws GeneralException
    {

        List<Warmup> ret = new ArrayList ();

        try
        {

            ResultSet rs = this.objectManager.executeQuery ("SELECT dbkey, chapterdbkey, promptid FROM warmup_v",
                                                            null,
                                                            conn);

            while (rs.next ())
            {

                ret.add (this.getWarmup (rs,
                                         (Project) parent));

            }

            try
            {

                rs.close ();

            } catch (Exception e)
            {
            }

        } catch (Exception e)
        {

            throw new GeneralException ("Unable to load warmups for: " +
                                        parent,
                                        e);

        }

        return ret;

    }

    public NamedObject getObjectByKey (int        key,
                                       Connection conn,
                                       boolean    loadChildObjects)
                                throws GeneralException
    {

        Warmup w = null;

        try
        {

            List params = new ArrayList ();
            params.add (key);

            ResultSet rs = this.objectManager.executeQuery ("SELECT dbkey, chapterdbkey, promptid FROM warmup_v WHERE dbkey = ?",
                                                            params,
                                                            conn);

            if (rs.next ())
            {

                w = this.getWarmup (rs,
                                    null);

            }

            try
            {

                rs.close ();

            } catch (Exception e)
            {
            }

        } catch (Exception e)
        {

            throw new GeneralException ("Unable to load warmup for key: " +
                                        key,
                                        e);

        }

        return w;

    }

    public void createObject (DataObject d,
                              Connection conn)
                       throws GeneralException
    {

        Warmup w = (Warmup) d;

        List params = new ArrayList ();
        params.add (w.getKey ());
        params.add (w.getChapter ().getKey ());
        params.add (w.getPrompt ().getId ());

        this.objectManager.executeStatement ("INSERT INTO warmup (dbkey, chapterdbkey, promptid) VALUES (?, ?, ?)",
                                             params,
                                             conn);

    }

    public void deleteObject (DataObject d,
                              boolean    deleteChildObjects,                              
                              Connection conn)
                       throws GeneralException
    {

        Warmup w = (Warmup) d;

        List params = new ArrayList ();
        params.add (w.getKey ());

        this.objectManager.executeStatement ("DELETE FROM warmup WHERE dbkey = ?",
                                             params,
                                             conn);

    }

    public void updateObject (DataObject d,
                              Connection conn)
                       throws GeneralException
    {

        Warmup w = (Warmup) d;

        List params = new ArrayList ();
        params.add (w.getChapter ().getKey ());
        params.add (w.getPrompt ().getId ());
        params.add (w.getKey ());

        this.objectManager.executeStatement ("UPDATE warmup SET chapterdbkey = ?, promptid = ? WHERE dbkey = ?",
                                             params,
                                             conn);

    }

}