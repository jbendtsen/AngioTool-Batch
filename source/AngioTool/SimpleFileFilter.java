package AngioTool;

import java.io.File;
import javax.swing.filechooser.FileFilter;

// This file is almost not necessary, since there already exists a class called
// FileNameExtensionFilter in javax.swing.filechooser that accomplishes the same goal.
// However, that class does not display which extensions are accepted in its description.

public class SimpleFileFilter extends FileFilter
{
    public String description;
    public String[] extList;

    public SimpleFileFilter(String description, String... extensions)
    {
        super();

        String[] exts = extensions;
        this.description = description;
        this.extList = new String[exts.length];

        for (int i = 0; i < exts.length; i++)
            extList[i] = exts[i].charAt(0) == '.' ? exts[i] : ("." + exts[i]);
    }

    @Override
    public boolean accept(File f)
    {
        String name = f.getName();
        if (!f.isFile())
            return false;

        for (int i = 0; i < extList.length; i++) {
            if (name.endsWith(extList[i]))
                return true;
        }

        return false;
    }

    @Override
    public String getDescription()
    {
        String allExts = String.join(", ", extList);
        if (description == null || description.length() == 0)
            return allExts;

        return description + " (" + allExts + ")";
    }

    public String tailorFileName(String name)
    {
        if (extList.length == 0)
            return name;

        for (int i = 0; i < extList.length; i++) {
            if (name.endsWith(extList[i]))
                return name;
        }

        return name + extList[0];
    }
}
