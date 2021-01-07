package codechicken.multipart.api.part;

import codechicken.multipart.api.annotation.MultiPartMarker;
import codechicken.multipart.trait.TTickableTile;

/**
 * Created by covers1624 on 18/9/20.
 */
@MultiPartMarker (TTickableTile.class)
public interface ITickablePart {

    void tick();

}
