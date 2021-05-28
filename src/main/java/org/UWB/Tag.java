/*
 * SPDX-License-Identifier: Apache-2.0
 */
/**
* @author  Sachith Liyanagama
* @since   2021-04-15
*/

package org.UWB;

import org.hyperledger.fabric.contract.annotation.DataType;
import org.hyperledger.fabric.contract.annotation.Property;
import com.owlike.genson.Genson;
import com.owlike.genson.annotation.JsonDateFormat;
import com.owlike.genson.annotation.JsonProperty;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

@DataType()
public class Tag {

    public enum TagCategory {
        Human, AGV, Tools, Pallette, Forklift
    };

    private final static Genson genson = new Genson();

    // #region AnchorState
    // Anchor State Class
    public static class AnchorState {
        private String anchorID;
        private double distance;

        /**
         * CONSTRUCT : Consutrctor for the Anchor State Immutable Class.
         * 
         * @param anchorID : String | Identification of the Anchor
         * @param distance : double | Distance to the Anchor from the Tag.
         */
        AnchorState(@JsonProperty("anchorID") String anchorID, @JsonProperty("distance") double distance) {
            this.anchorID = anchorID;
            this.distance = distance;
        }

        /**
         * @return String return the anchorID
         */
        public String getAnchorID() {
            return anchorID;
        }

        /**
         * @return double return the distance
         */
        public double getDistance() {
            return distance;
        }
    }
    // #endregionendregion

    // #region Properties
    @Property()
    @JsonDateFormat(value="yyyy-MM-dd HH:mm:ss")
    private LocalDateTime timestamp;

    @Property(schema = { "pattern", "^[a-zA-Z]{0,10}$" })
    private String tagID;

    @Property()
    private String tagName;

    @Property()
    private AnchorState anchor1;

    @Property()
    private AnchorState anchor2;

    @Property()
    private AnchorState anchor3;

    @Property()
    private AnchorState anchor4;

    @Property()
    private TagCategory category;
    // #endregion

    public Tag() {
    }

    public Tag(String tagID, String time, String tagName, String cat) {
        this.tagID = tagID;
        this.tagName = tagName;
        this.category = TagCategory.valueOf(cat);
        DateTimeFormatter format = DateTimeFormatter.ofPattern("dd-MMM-yyyy HH:mm:ss");
        this.timestamp = LocalDateTime.parse(time, format);
    }

    /**
     * FUNC : Serialize the Class and its Data.
     * 
     * @return class : String | Serilaized Class of the Tag.
     */
    public String toJSONString() {
        return genson.serialize(this).toString();
    }

    /**
     * FUNC : Desrialize a string and create an instance of a NEW Tag
     * 
     * @param json : String | JSON string to be Deserialized.
     * @return asset : Tag | Newly created object of the Tag.
     */
    public static Tag fromJSONString(String json) {
        Tag asset = genson.deserialize(json, Tag.class);
        return asset;
    }

    // #region Getters
    /**
     * @return Date return the timestamp
     */
    public String getTimestamp() {
        DateTimeFormatter format = DateTimeFormatter.ofPattern("dd-MMM-yyyy HH:mm:ss");
        return this.timestamp.format(format);
    }

    /**
     * @return String return the tagID
     */
    public String getTagID() {
        return tagID;
    }

    /**
     * @return String return the tagName
     */
    public String getTagName() {
        return tagName;
    }

    /**
     * @return String return the category
     */
    public String getCategory() {
        return category.name();
    }

    /**
     * @return AnchorState return the anchor1
     */
    public AnchorState getAnchor1() {
        return anchor1;
    }

    /**
     * @return AnchorState return the anchor2
     */
    public AnchorState getAnchor2() {
        return anchor2;
    }

    /**
     * @return AnchorState return the anchor3
     */
    public AnchorState getAnchor3() {
        return anchor3;
    }

    /**
     * @return AnchorState return the anchor4
     */
    public AnchorState getAnchor4() {
        return anchor4;
    }
    // #endregion

    // #region Setters
    /**
     * @param timestamp the timestamp to set
     */
    public void setTimestamp(String time) {
        DateTimeFormatter format = DateTimeFormatter.ofPattern("dd-MMM-yyyy HH:mm:ss");
        this.timestamp = LocalDateTime.parse(time, format);
    }

    /**
     * @param tagID the tagID to set
     */
    public void setTagID(String tagID) {
        this.tagID = tagID;
    }

    /**
     * @param tagName the tagName to set
     */
    public void setTagName(String tagName) {
        this.tagName = tagName;
    }

    /**
     * @param category the category to set
     */
    public void setCategory(String cat) {
        this.category = TagCategory.valueOf(cat);
    }

    /**
     * @param anchor1 the anchor1 to set
     */
    public void setAnchor1(AnchorState anchor1) {
        this.anchor1 = anchor1;
    }

    /**
     * @param anchor2 the anchor2 to set
     */
    public void setAnchor2(AnchorState anchor2) {
        this.anchor2 = anchor2;
    }

    /**
     * @param anchor3 the anchor3 to set
     */
    public void setAnchor3(AnchorState anchor3) {
        this.anchor3 = anchor3;
    }

    /**
     * @param anchor4 the anchor4 to set
     */
    public void setAnchor4(AnchorState anchor4) {
        this.anchor4 = anchor4;
    }

    // #endregion

}
