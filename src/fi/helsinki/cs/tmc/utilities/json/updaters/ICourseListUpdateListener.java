/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package fi.helsinki.cs.tmc.utilities.json.updaters;

/**
 *
 * @author knordman
 */
@Deprecated
public interface ICourseListUpdateListener {
    public void courseListDownloadComplete();
    public void courseListDownloadFailed(String errorMessage);
}
