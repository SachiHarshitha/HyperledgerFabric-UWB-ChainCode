/*
 * SPDX-License-Identifier: Apache-2.0
 */
/**
* @author  Sachith Liyanagama
* @since   2021-04-15
*/
package org.UWB;

import org.hyperledger.fabric.contract.Context;
import org.hyperledger.fabric.contract.ContractInterface;
import org.hyperledger.fabric.contract.annotation.Contract;
import org.hyperledger.fabric.contract.annotation.Default;
import org.hyperledger.fabric.contract.annotation.Transaction;
import org.hyperledger.fabric.contract.annotation.Contact;
import org.hyperledger.fabric.contract.annotation.Info;
import org.hyperledger.fabric.contract.annotation.License;
import static java.nio.charset.StandardCharsets.UTF_8;
import org.hyperledger.fabric.shim.ledger.KeyModification;
import org.hyperledger.fabric.shim.ledger.KeyValue;
import org.hyperledger.fabric.shim.ledger.QueryResultsIterator;
import java.util.ArrayList;
import java.util.Iterator;
import org.hyperledger.fabric.shim.ChaincodeException;
import com.owlike.genson.Genson;
import org.hyperledger.fabric.shim.ChaincodeStub;

@Contract(name = "TagPositionContract", info = @Info(title = "TagPosition contract", description = "Smart Contract for asset UWB Tag.", version = "0.0.1", license = @License(name = "Apache-2.0", url = ""), contact = @Contact(email = "sachi.harshitha@live.com", name = "UWBPositioning", url = "http://UWBPositioning.me")))
@Default
public class TagContract implements ContractInterface {

    private final static Genson genson = new Genson();

    public TagContract() {

    }

    /** QUERY : Check if Tag exists in the Chain
     * 
     * @param ctx : Context | Fabric Current Context
     * @param tagId : String | Tag Identification Code (Ex. Device ID)
     * @return status : boolean | Boolean value depicting the existence
     */
    @Transaction()
    public boolean tagExists(Context ctx, String tagId) {
        byte[] buffer = ctx.getStub().getState(tagId);
        return (buffer != null && buffer.length > 0);
    }

    /** TRANS : Register A new Tag
     * 
     * @param ctx : Context | Fabric Current Context
     * @param tagId : String | Tag Identification Code (Ex. Device ID)
     * @param timestamp : Date | Timestamp as an input value.
     * @param tagName : String | Name Of the Tag (Ex. 19328CF01010FTS1)
     * @param category : String | Category of the Object bound to this Tag
     */
    @Transaction()
    public void createTag(Context ctx, String tagId, String timestamp, String tagName, String category) {
        boolean exists = tagExists(ctx, tagId);
        if (exists) {
            throw new RuntimeException("The asset " + tagId + " already exists");
        }
        try {

            Tag asset = new Tag(tagId, timestamp, tagName, category);
            ctx.getStub().putState(tagId, asset.toJSONString().getBytes(UTF_8));

        } catch (Exception e) {
            System.out.println(e.toString());
            throw new ChaincodeException("Error", e.toString());
        }
    }

    /** QUERY : Get the History of a particular Tag
     * 
     * @param ctx : Context | Fabric Current Context
     * @param tagId : String | Tag Identification Code (Ex. Device ID)
     * @return history : String | Historical records concatenated into a JSON string.
     */
    @Transaction
    public String queryTagHistory(final Context ctx, final String tagId) {
        if (tagId == null) {
            throw new RuntimeException("No ID given");
        }
        ChaincodeStub stub = ctx.getStub();
        ArrayList<String> results = new ArrayList<>();
        try {
            QueryResultsIterator<KeyModification> history = stub.getHistoryForKey(tagId);

            if (history == null) {
                String errorMessage = String.format("Product %s does not exist", tagId);
                System.out.println(errorMessage);
                throw new ChaincodeException(errorMessage, "Position not found");
            }
            Iterator<KeyModification> iterator = history.iterator();
            while (iterator.hasNext()) {
                String iteratorValue = iterator.next().getStringValue();
                results.add(iteratorValue);
                System.out.println(iteratorValue);
            }
            history.close();
        } catch (Exception e) {
            results.add(e.getMessage());
            results.add(e.getCause().getMessage());
            results.add(e.getStackTrace().toString());
        }
        return results.toString();
    }

    /** QUERY : Get the Current Tag Information from World State
     * 
     * @param ctx : Context | Fabric Current Context
     * @param tagId : String | Tag Identification Code (Ex. Device ID)
     * @return info : String | Tag Information Concatenated into a JSON String.
     */
    @Transaction()
    public Tag getTag(final Context ctx, final String tagId) {
        ChaincodeStub stub = ctx.getStub();
        String positionState = stub.getStringState(tagId);

        if (positionState.isEmpty()) {
            String errorMessage = String.format("Position %s does not exist", tagId);
            System.out.println(errorMessage);
            throw new ChaincodeException(errorMessage, "Position not found");
        }

        Tag position = genson.deserialize(positionState, Tag.class);

        return position;
    }

    /** TRANS : Update the Tag Name
     * 
     * @param ctx : Context | Fabric Current Context
     * @param tagId : String | Tag Identification Code (Ex. Device ID)
     * @param timestamp : Date | Timestamp as an input value.
     * @param tagName : String | !!!NEW!!! Name Of the Tag (Ex. 19328CF01010FTS1)
     */
    @Transaction()
    public void updateTagName(Context ctx, String tagId, String timestamp, String tagName) {
        boolean exists = tagExists(ctx, tagId);
        if (!exists) {
            throw new RuntimeException("The asset " + tagId + " does not exist");
        }
        try {
            ChaincodeStub stub = ctx.getStub();
            String tagJSON = stub.getStringState(tagId);
            Tag tag = genson.deserialize(tagJSON, Tag.class);

            Tag newTag = new Tag();
            newTag.setTimestamp(timestamp);
            newTag.setTagID(tagId);
            newTag.setTagName(tagName);
            newTag.setCategory(tag.getCategory());

            String newTagJSON = genson.serialize(newTag);
            stub.putStringState(tagId, newTagJSON);
        } catch (Exception e) {
            System.out.println(e.toString());
            throw new ChaincodeException("Error", e.toString());
        }
    }

    /** TRANS : Update the Current Location of the Tag
     * 
     * @param ctx : Context | Fabric Current Context
     * @param tagId : String | Tag Identification Code (Ex. Device ID)
     * @param timestamp : Date | Timestamp as an input value.
     * @param anchor1ID : String | Identification of the FIRST Anchor
     * @param distAnchor1 : double | Distance to the FIRST Anchor
     * @param anchor2ID : String | Identification of the SECOND Anchor
     * @param distAnchor2 : double | Distance to the SECOND Anchor
     * @param anchor3ID : String | Identification of the THIRD Anchor
     * @param distAnchor3 : double | Distance to the THIRD Anchor
     * @param anchor4ID : String | Identification of the FOURTH Anchor
     * @param distAnchor4 : double | Distance to the FOURTH Anchor
     * @return log : String | Exception information incase thrown.
     */
    @Transaction()
    public String updateTagPos(Context ctx, String tagId, String timestamp, String anchor1ID, double distAnchor1,
            String anchor2ID, double distAnchor2, String anchor3ID, double distAnchor3, String anchor4ID,
            double distAnchor4) {
        boolean exists = tagExists(ctx, tagId);
        ArrayList<String> results = new ArrayList<>();
        if (!exists) {
            throw new RuntimeException("The asset " + tagId + " does not exist");
        }
        try {
            ChaincodeStub stub = ctx.getStub();
            String tagJSON = stub.getStringState(tagId);
            Tag tag = genson.deserialize(tagJSON, Tag.class);
            Tag newTag = new Tag();
            newTag.setTimestamp(timestamp);
            newTag.setTagID(tagId);
            newTag.setAnchor1(new Tag.AnchorState(anchor1ID, distAnchor1));
            newTag.setAnchor2(new Tag.AnchorState(anchor2ID, distAnchor2));
            newTag.setAnchor3(new Tag.AnchorState(anchor3ID, distAnchor3));
            newTag.setAnchor4(new Tag.AnchorState(anchor4ID, distAnchor4));
            newTag.setTagName(tag.getTagName());
            newTag.setCategory(tag.getCategory());

            String newTagJSON = genson.serialize(newTag);
            stub.putStringState(tagId, newTagJSON);
        } catch (Exception e) {
            results.add(e.getMessage());
            results.add(e.getCause().getMessage());
            results.add(e.getStackTrace().toString());
            throw new ChaincodeException("Error", e.toString());
        }
        return results.toString();
    }

    /** TRANS : Update the Category of the Tag
     * 
     * @param ctx : Context | Fabric Current Context
     * @param tagId : String | Tag Identification Code (Ex. Device ID)
     * @param timestamp : Date | Timestamp as an input value.
     * @param category : String | !!!NEW!!! Category of the Object bound to this Tag
     * @return log : String | Exception information incase thrown.
     */
    @Transaction()
    public String updateTagCat(Context ctx, String tagId, String timestamp, String category) {
        ArrayList<String> results = new ArrayList<>();
        boolean exists = tagExists(ctx, tagId);
        if (!exists) {
            throw new RuntimeException("The asset " + tagId + " does not exist");
        }
        try {
            ChaincodeStub stub = ctx.getStub();
            String tagJSON = stub.getStringState(tagId);
            Tag tag = genson.deserialize(tagJSON, Tag.class);
            Tag newTag = new Tag();
            newTag.setTimestamp(timestamp);
            newTag.setTagID(tagId);
            newTag.setCategory(category);
            newTag.setTagName(tag.getTagName());

            String newTagJSON = genson.serialize(newTag);
            stub.putStringState(tagId, newTagJSON);
        } catch (Exception e) {
            results.add(e.getMessage());
            results.add(e.getCause().getMessage());
            results.add(e.getStackTrace().toString());
            throw new ChaincodeException("Error", e.toString());
        }
        return results.toString();
    }

    /** TRANS : Delete a Tag from the Blockchain
     *  
     * @param ctx : Context | Fabric Current Context
     * @param tagId : String | Tag Identification Code (Ex. Device ID)
     */
    @Transaction()
    public void deleteTag(Context ctx, String tagId) {
        boolean exists = tagExists(ctx, tagId);
        if (!exists) {
            throw new RuntimeException("The asset " + tagId + " does not exist");
        }
        ctx.getStub().delState(tagId);
    }

    /** QUERY : Get all the Tags Registered in the network.
     * 
     * @param ctx : Context | Fabric Current Context
     * @return TAGS : String | All the TAG IDs Concatenated into a JSON String.
     */
    @Transaction()
    public String getAllTags(Context ctx) {
        ChaincodeStub stub = ctx.getStub();
        QueryResultsIterator<KeyValue> queryIt = stub.getStateByRange("", "");
        ArrayList<String> results = new ArrayList<>();
        if (queryIt == null) {
            String errorMessage = "There are no Tags Available.";
            System.out.println(errorMessage);
            throw new ChaincodeException(errorMessage, "Empty ChainCode");
        }
        try {
            Iterator<KeyValue> iterator = queryIt.iterator();
            while (iterator.hasNext()) {
                String key = iterator.next().getKey();
                results.add(key);
                System.out.println(key);
            }
            queryIt.close();
        } catch (Exception e) {
            results.add(e.getMessage());
            results.add(e.getCause().getMessage());
            results.add(e.getStackTrace().toString());
        }
        return results.toString();
    }

    /** QUERY : Get the Current Position of a Tag
     * 
     * @param ctx : Context | Fabric Current Context
     * @param tagId : String | Tag Identification Code (Ex. Device ID)
     * @return AnchorStates : String | All the Anchors and their respective distances Concatenated into a JSON String.
     */
    @Transaction()
    public String getPosition(Context ctx, String tagId) {
        ChaincodeStub stub = ctx.getStub();
        ArrayList<String> results = new ArrayList<>();

        try {
            String tagJSON = stub.getStringState(tagId);
            Tag tag = genson.deserialize(tagJSON, Tag.class);
            results.add(genson.serialize(tag.getAnchor1()));
            results.add(genson.serialize(tag.getAnchor2()));
            results.add(genson.serialize(tag.getAnchor3()));
            results.add(genson.serialize(tag.getAnchor4()));
        } catch (Exception e) {
            results.add(e.getMessage());
            results.add(e.getCause().getMessage());
            results.add(e.getStackTrace().toString());
        }
        return results.toString();
    }

}
