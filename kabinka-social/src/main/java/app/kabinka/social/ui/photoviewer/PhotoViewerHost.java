package app.kabinka.social.ui.photoviewer;

import app.kabinka.social.model.Status;
import app.kabinka.social.ui.displayitems.MediaGridStatusDisplayItem;

public interface PhotoViewerHost{
	void openPhotoViewer(String parentID, Status status, int attachmentIndex, MediaGridStatusDisplayItem.Holder gridHolder);
}
