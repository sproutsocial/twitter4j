/*
 * Copyright 2007 Yusuke Yamamoto
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package twitter4j;

import twitter4j.conf.Configuration;

import java.util.Arrays;
import java.util.Date;

/**
 * A data class representing sent/received direct message.
 *
 * @author Yusuke Yamamoto - yusuke at mac.com
 */
/*package*/ final class DirectMessageJSONImpl extends TwitterResponseImpl implements DirectMessage, java.io.Serializable {
    private static final long serialVersionUID = 7092906238192790921L;
    private long id;
    private String text;
    private long senderId;
    private long recipientId;
    private Date createdAt;
    private String senderScreenName;
    private String recipientScreenName;
    private UserMentionEntity[] userMentionEntities;
    private URLEntity[] urlEntities;
    private HashtagEntity[] hashtagEntities;
    private MediaEntity[] mediaEntities;
    private SymbolEntity[] symbolEntities;


    /*package*/DirectMessageJSONImpl(HttpResponse res, Configuration conf) throws TwitterException {
        super(res);
        JSONObject json = res.asJSONObject();
        init(json);
        if (conf.isJSONStoreEnabled()) {
            TwitterObjectFactory.clearThreadLocalMap();
            TwitterObjectFactory.registerJSONObject(this, json);
        }
    }

    /*package*/DirectMessageJSONImpl(JSONObject json) throws TwitterException {
        init(json);
    }

    private void init(JSONObject json) throws TwitterException {
        id = ParseUtil.getLong("id", json);
        senderId = ParseUtil.getLong("sender_id", json);
        recipientId = ParseUtil.getLong("recipient_id", json);
        createdAt = ParseUtil.getDate("created_at", json);
        senderScreenName = ParseUtil.getUnescapedString("sender_screen_name", json);
        recipientScreenName = ParseUtil.getUnescapedString("recipient_screen_name", json);
        try {
            sender = new UserJSONImpl(json.getJSONObject("sender"));
            recipient = new UserJSONImpl(json.getJSONObject("recipient"));
            if (!json.isNull("entities")) {
                JSONObject entities = json.getJSONObject("entities");
                userMentionEntities = EntitiesParseUtil.getUserMentions(entities);
                urlEntities = EntitiesParseUtil.getUrls(entities);
                hashtagEntities = EntitiesParseUtil.getHashtags(entities);
                symbolEntities = EntitiesParseUtil.getSymbols(entities);
                mediaEntities = EntitiesParseUtil.getMedia(entities);
            }
            userMentionEntities = userMentionEntities == null ? new UserMentionEntity[0] : userMentionEntities;
            urlEntities = urlEntities == null ? new URLEntity[0] : urlEntities;
            hashtagEntities = hashtagEntities == null ? new HashtagEntity[0] : hashtagEntities;
            symbolEntities = symbolEntities == null ? new SymbolEntity[0] : symbolEntities;
            mediaEntities = mediaEntities == null ? new MediaEntity[0] : mediaEntities;
            text = HTMLEntity.unescapeAndSlideEntityIncdices(json.getString("text"), userMentionEntities,
                    urlEntities, hashtagEntities, mediaEntities);
        } catch (JSONException jsone) {
            throw new TwitterException(jsone);
        }
    }

    @Override
    public long getId() {
        return id;
    }

    @Override
    public String getText() {
        return text;
    }

    @Override
    public long getSenderId() {
        return senderId;
    }

    @Override
    public long getRecipientId() {
        return recipientId;
    }

    @Override
    public Date getCreatedAt() {
        return createdAt;
    }

    @Override
    public String getSenderScreenName() {
        return senderScreenName;
    }

    @Override
    public String getRecipientScreenName() {
        return recipientScreenName;
    }

    private User sender;

    @Override
    public User getSender() {
        return sender;
    }

    private User recipient;

    @Override
    public User getRecipient() {
        return recipient;
    }

    @Override
    public UserMentionEntity[] getUserMentionEntities() {
        return userMentionEntities;
    }

    @Override
    public URLEntity[] getURLEntities() {
        return urlEntities;
    }

    @Override
    public HashtagEntity[] getHashtagEntities() {
        return hashtagEntities;
    }

    @Override
    public MediaEntity[] getMediaEntities() {
        return mediaEntities;
    }

    @Override
    public SymbolEntity[] getSymbolEntities() {
        return symbolEntities;
    }

    /*package*/
    static ResponseList<DirectMessage> createDirectMessageList(HttpResponse res, Configuration conf) throws TwitterException {
        try {
            if (conf.isJSONStoreEnabled()) {
                TwitterObjectFactory.clearThreadLocalMap();
            }
            JSONArray list = res.asJSONArray();
            int size = list.length();
            ResponseList<DirectMessage> directMessages = new ResponseListImpl<DirectMessage>(size, res);
            for (int i = 0; i < size; i++) {
                JSONObject json = list.getJSONObject(i);
                DirectMessage directMessage = new DirectMessageJSONImpl(json);
                directMessages.add(directMessage);
                if (conf.isJSONStoreEnabled()) {
                    TwitterObjectFactory.registerJSONObject(directMessage, json);
                }
            }
            if (conf.isJSONStoreEnabled()) {
                TwitterObjectFactory.registerJSONObject(directMessages, list);
            }
            return directMessages;
        } catch (JSONException jsone) {
            throw new TwitterException(jsone);
        }
    }

    /*package*/
    static DirectMessageEventList createDirectMessageEventList(HttpResponse res, Configuration conf) throws TwitterException {
        try {
            if (conf.isJSONStoreEnabled()) {
                TwitterObjectFactory.clearThreadLocalMap();
            }
            JSONObject jsonObject = res.asJSONObject();
            JSONArray list = jsonObject.getJSONArray("events");
            int size = list.length();
            DirectMessageEventList directMessages = new DirectMessageEventListImpl(size, jsonObject, res);
            for (int i = 0; i < size; i++) {
                JSONObject json = list.getJSONObject(i);
                DirectMessageEvent directMessage = new DirectMessageEventJSONImpl(json);
                directMessages.add(directMessage);
                if (conf.isJSONStoreEnabled()) {
                    TwitterObjectFactory.registerJSONObject(directMessage, json);
                }
            }
            if (conf.isJSONStoreEnabled()) {
                TwitterObjectFactory.registerJSONObject(directMessages, list);
            }
            return directMessages;
        } catch (JSONException jsone) {
            throw new TwitterException(jsone);
        }
    }

    @Override
    public int hashCode() {
        return (int) id;
    }

    @Override
    public boolean equals(Object obj) {
        if (null == obj) {
            return false;
        }
        if (this == obj) {
            return true;
        }
        return obj instanceof DirectMessage && ((DirectMessage) obj).getId() == this.id;
    }

    @Override
    public String toString() {
        return "DirectMessageJSONImpl{" +
                "id=" + id +
                ", text='" + text + '\'' +
                ", sender_id=" + senderId +
                ", recipient_id=" + recipientId +
                ", created_at=" + createdAt +
                ", userMentionEntities=" + (userMentionEntities == null ? null : Arrays.asList(userMentionEntities)) +
                ", urlEntities=" + (urlEntities == null ? null : Arrays.asList(urlEntities)) +
                ", hashtagEntities=" + (hashtagEntities == null ? null : Arrays.asList(hashtagEntities)) +
                ", sender_screen_name='" + senderScreenName + '\'' +
                ", recipient_screen_name='" + recipientScreenName + '\'' +
                ", sender=" + sender +
                ", recipient=" + recipient +
                ", userMentionEntities=" + (userMentionEntities == null ? null : Arrays.asList(userMentionEntities)) +
                ", urlEntities=" + (urlEntities == null ? null : Arrays.asList(urlEntities)) +
                ", hashtagEntities=" + (hashtagEntities == null ? null : Arrays.asList(hashtagEntities)) +
                '}';
    }
}
