struct IDiscordLobbyTransaction {
    enum EDiscordResult (*set_type)(struct IDiscordLobbyTransaction* lobby_transaction, enum EDiscordLobbyType type);
    enum EDiscordResult (*set_owner)(struct IDiscordLobbyTransaction* lobby_transaction, DiscordUserId owner_id);
    enum EDiscordResult (*set_capacity)(struct IDiscordLobbyTransaction* lobby_transaction, uint32_t capacity);
    enum EDiscordResult (*set_metadata)(struct IDiscordLobbyTransaction* lobby_transaction, DiscordMetadataKey key, DiscordMetadataValue value);
    enum EDiscordResult (*delete_metadata)(struct IDiscordLobbyTransaction* lobby_transaction, DiscordMetadataKey key);
    enum EDiscordResult (*set_locked)(struct IDiscordLobbyTransaction* lobby_transaction, bool locked);
};

struct IDiscordLobbyMemberTransaction {
    enum EDiscordResult (*set_metadata)(struct IDiscordLobbyMemberTransaction* lobby_member_transaction, DiscordMetadataKey key, DiscordMetadataValue value);
    enum EDiscordResult (*delete_metadata)(struct IDiscordLobbyMemberTransaction* lobby_member_transaction, DiscordMetadataKey key);
};

struct IDiscordLobbySearchQuery {
    enum EDiscordResult (*filter)(struct IDiscordLobbySearchQuery* lobby_search_query, DiscordMetadataKey key, enum EDiscordLobbySearchComparison comparison, enum EDiscordLobbySearchCast cast, DiscordMetadataValue value);
    enum EDiscordResult (*sort)(struct IDiscordLobbySearchQuery* lobby_search_query, DiscordMetadataKey key, enum EDiscordLobbySearchCast cast, DiscordMetadataValue value);
    enum EDiscordResult (*limit)(struct IDiscordLobbySearchQuery* lobby_search_query, uint32_t limit);
    enum EDiscordResult (*distance)(struct IDiscordLobbySearchQuery* lobby_search_query, enum EDiscordLobbySearchDistance distance);
};

typedef void* IDiscordApplicationEvents;

struct IDiscordApplicationManager {
    void (*validate_or_exit)(struct IDiscordApplicationManager* manager, void* callback_data, void (*callback)(void* callback_data, enum EDiscordResult result));
    void (*get_current_locale)(struct IDiscordApplicationManager* manager, DiscordLocale* locale);
    void (*get_current_branch)(struct IDiscordApplicationManager* manager, DiscordBranch* branch);
    void (*get_oauth2_token)(struct IDiscordApplicationManager* manager, void* callback_data, void (*callback)(void* callback_data, enum EDiscordResult result, struct DiscordOAuth2Token* oauth2_token));
    void (*get_ticket)(struct IDiscordApplicationManager* manager, void* callback_data, void (*callback)(void* callback_data, enum EDiscordResult result, const char* data));
};

struct IDiscordUserEvents {
    void (*on_current_user_update)(void* event_data);
};

struct IDiscordUserManager {
    enum EDiscordResult (*get_current_user)(struct IDiscordUserManager* manager, struct DiscordUser* current_user);
    void (*get_user)(struct IDiscordUserManager* manager, DiscordUserId user_id, void* callback_data, void (*callback)(void* callback_data, enum EDiscordResult result, struct DiscordUser* user));
    enum EDiscordResult (*get_current_user_premium_type)(struct IDiscordUserManager* manager, enum EDiscordPremiumType* premium_type);
    enum EDiscordResult (*current_user_has_flag)(struct IDiscordUserManager* manager, enum EDiscordUserFlag flag, bool* has_flag);
};

typedef void* IDiscordImageEvents;

struct IDiscordImageManager {
    void (*fetch)(struct IDiscordImageManager* manager, struct DiscordImageHandle handle, bool refresh, void* callback_data, void (*callback)(void* callback_data, enum EDiscordResult result, struct DiscordImageHandle handle_result));
    enum EDiscordResult (*get_dimensions)(struct IDiscordImageManager* manager, struct DiscordImageHandle handle, struct DiscordImageDimensions* dimensions);
    enum EDiscordResult (*get_data)(struct IDiscordImageManager* manager, struct DiscordImageHandle handle, uint8_t* data, uint32_t data_length);
};

struct IDiscordActivityEvents {
    void (*on_activity_join)(void* event_data, const char* secret);
    void (*on_activity_spectate)(void* event_data, const char* secret);
    void (*on_activity_join_request)(void* event_data, struct DiscordUser* user);
    void (*on_activity_invite)(void* event_data, enum EDiscordActivityActionType type, struct DiscordUser* user, struct DiscordActivity* activity);
};

struct IDiscordActivityManager {
    enum EDiscordResult (*register_command)(struct IDiscordActivityManager* manager, const char* command);
    enum EDiscordResult (*register_steam)(struct IDiscordActivityManager* manager, uint32_t steam_id);
    void (*update_activity)(struct IDiscordActivityManager* manager, struct DiscordActivity* activity, void* callback_data, void (*callback)(void* callback_data, enum EDiscordResult result));
    void (*clear_activity)(struct IDiscordActivityManager* manager, void* callback_data, void (*callback)(void* callback_data, enum EDiscordResult result));
    void (*send_request_reply)(struct IDiscordActivityManager* manager, DiscordUserId user_id, enum EDiscordActivityJoinRequestReply reply, void* callback_data, void (*callback)(void* callback_data, enum EDiscordResult result));
    void (*send_invite)(struct IDiscordActivityManager* manager, DiscordUserId user_id, enum EDiscordActivityActionType type, const char* content, void* callback_data, void (*callback)(void* callback_data, enum EDiscordResult result));
    void (*accept_invite)(struct IDiscordActivityManager* manager, DiscordUserId user_id, void* callback_data, void (*callback)(void* callback_data, enum EDiscordResult result));
};

struct IDiscordRelationshipEvents {
    void (*on_refresh)(void* event_data);
    void (*on_relationship_update)(void* event_data, struct DiscordRelationship* relationship);
};

struct IDiscordRelationshipManager {
    void (*filter)(struct IDiscordRelationshipManager* manager, void* filter_data, bool (*filter)(void* filter_data, struct DiscordRelationship* relationship));
    enum EDiscordResult (*count)(struct IDiscordRelationshipManager* manager, int32_t* count);
    enum EDiscordResult (*get)(struct IDiscordRelationshipManager* manager, DiscordUserId user_id, struct DiscordRelationship* relationship);
    enum EDiscordResult (*get_at)(struct IDiscordRelationshipManager* manager, uint32_t index, struct DiscordRelationship* relationship);
};

struct IDiscordLobbyEvents {
    void (*on_lobby_update)(void* event_data, int64_t lobby_id);
    void (*on_lobby_delete)(void* event_data, int64_t lobby_id, uint32_t reason);
    void (*on_member_connect)(void* event_data, int64_t lobby_id, int64_t user_id);
    void (*on_member_update)(void* event_data, int64_t lobby_id, int64_t user_id);
    void (*on_member_disconnect)(void* event_data, int64_t lobby_id, int64_t user_id);
    void (*on_lobby_message)(void* event_data, int64_t lobby_id, int64_t user_id, uint8_t* data, uint32_t data_length);
    void (*on_speaking)(void* event_data, int64_t lobby_id, int64_t user_id, bool speaking);
    void (*on_network_message)(void* event_data, int64_t lobby_id, int64_t user_id, uint8_t channel_id, uint8_t* data, uint32_t data_length);
};

struct IDiscordLobbyManager {
    enum EDiscordResult (*get_lobby_create_transaction)(struct IDiscordLobbyManager* manager, struct IDiscordLobbyTransaction** transaction);
    enum EDiscordResult (*get_lobby_update_transaction)(struct IDiscordLobbyManager* manager, DiscordLobbyId lobby_id, struct IDiscordLobbyTransaction** transaction);
    enum EDiscordResult (*get_member_update_transaction)(struct IDiscordLobbyManager* manager, DiscordLobbyId lobby_id, DiscordUserId user_id, struct IDiscordLobbyMemberTransaction** transaction);
    void (*create_lobby)(struct IDiscordLobbyManager* manager, struct IDiscordLobbyTransaction* transaction, void* callback_data, void (*callback)(void* callback_data, enum EDiscordResult result, struct DiscordLobby* lobby));
    void (*update_lobby)(struct IDiscordLobbyManager* manager, DiscordLobbyId lobby_id, struct IDiscordLobbyTransaction* transaction, void* callback_data, void (*callback)(void* callback_data, enum EDiscordResult result));
    void (*delete_lobby)(struct IDiscordLobbyManager* manager, DiscordLobbyId lobby_id, void* callback_data, void (*callback)(void* callback_data, enum EDiscordResult result));
    void (*connect_lobby)(struct IDiscordLobbyManager* manager, DiscordLobbyId lobby_id, DiscordLobbySecret secret, void* callback_data, void (*callback)(void* callback_data, enum EDiscordResult result, struct DiscordLobby* lobby));
    void (*connect_lobby_with_activity_secret)(struct IDiscordLobbyManager* manager, DiscordLobbySecret activity_secret, void* callback_data, void (*callback)(void* callback_data, enum EDiscordResult result, struct DiscordLobby* lobby));
    void (*disconnect_lobby)(struct IDiscordLobbyManager* manager, DiscordLobbyId lobby_id, void* callback_data, void (*callback)(void* callback_data, enum EDiscordResult result));
    enum EDiscordResult (*get_lobby)(struct IDiscordLobbyManager* manager, DiscordLobbyId lobby_id, struct DiscordLobby* lobby);
    enum EDiscordResult (*get_lobby_activity_secret)(struct IDiscordLobbyManager* manager, DiscordLobbyId lobby_id, DiscordLobbySecret* secret);
    enum EDiscordResult (*get_lobby_metadata_value)(struct IDiscordLobbyManager* manager, DiscordLobbyId lobby_id, DiscordMetadataKey key, DiscordMetadataValue* value);
    enum EDiscordResult (*get_lobby_metadata_key)(struct IDiscordLobbyManager* manager, DiscordLobbyId lobby_id, int32_t index, DiscordMetadataKey* key);
    enum EDiscordResult (*lobby_metadata_count)(struct IDiscordLobbyManager* manager, DiscordLobbyId lobby_id, int32_t* count);
    enum EDiscordResult (*member_count)(struct IDiscordLobbyManager* manager, DiscordLobbyId lobby_id, int32_t* count);
    enum EDiscordResult (*get_member_user_id)(struct IDiscordLobbyManager* manager, DiscordLobbyId lobby_id, int32_t index, DiscordUserId* user_id);
    enum EDiscordResult (*get_member_user)(struct IDiscordLobbyManager* manager, DiscordLobbyId lobby_id, DiscordUserId user_id, struct DiscordUser* user);
    enum EDiscordResult (*get_member_metadata_value)(struct IDiscordLobbyManager* manager, DiscordLobbyId lobby_id, DiscordUserId user_id, DiscordMetadataKey key, DiscordMetadataValue* value);
    enum EDiscordResult (*get_member_metadata_key)(struct IDiscordLobbyManager* manager, DiscordLobbyId lobby_id, DiscordUserId user_id, int32_t index, DiscordMetadataKey* key);
    enum EDiscordResult (*member_metadata_count)(struct IDiscordLobbyManager* manager, DiscordLobbyId lobby_id, DiscordUserId user_id, int32_t* count);
    void (*update_member)(struct IDiscordLobbyManager* manager, DiscordLobbyId lobby_id, DiscordUserId user_id, struct IDiscordLobbyMemberTransaction* transaction, void* callback_data, void (*callback)(void* callback_data, enum EDiscordResult result));
    void (*send_lobby_message)(struct IDiscordLobbyManager* manager, DiscordLobbyId lobby_id, uint8_t* data, uint32_t data_length, void* callback_data, void (*callback)(void* callback_data, enum EDiscordResult result));
    enum EDiscordResult (*get_search_query)(struct IDiscordLobbyManager* manager, struct IDiscordLobbySearchQuery** query);
    void (*search)(struct IDiscordLobbyManager* manager, struct IDiscordLobbySearchQuery* query, void* callback_data, void (*callback)(void* callback_data, enum EDiscordResult result));
    void (*lobby_count)(struct IDiscordLobbyManager* manager, int32_t* count);
    enum EDiscordResult (*get_lobby_id)(struct IDiscordLobbyManager* manager, int32_t index, DiscordLobbyId* lobby_id);
    void (*connect_voice)(struct IDiscordLobbyManager* manager, DiscordLobbyId lobby_id, void* callback_data, void (*callback)(void* callback_data, enum EDiscordResult result));
    void (*disconnect_voice)(struct IDiscordLobbyManager* manager, DiscordLobbyId lobby_id, void* callback_data, void (*callback)(void* callback_data, enum EDiscordResult result));
    enum EDiscordResult (*connect_network)(struct IDiscordLobbyManager* manager, DiscordLobbyId lobby_id);
    enum EDiscordResult (*disconnect_network)(struct IDiscordLobbyManager* manager, DiscordLobbyId lobby_id);
    enum EDiscordResult (*flush_network)(struct IDiscordLobbyManager* manager);
    enum EDiscordResult (*open_network_channel)(struct IDiscordLobbyManager* manager, DiscordLobbyId lobby_id, uint8_t channel_id, bool reliable);
    enum EDiscordResult (*send_network_message)(struct IDiscordLobbyManager* manager, DiscordLobbyId lobby_id, DiscordUserId user_id, uint8_t channel_id, uint8_t* data, uint32_t data_length);
};

struct IDiscordNetworkEvents {
    void (*on_message)(void* event_data, DiscordNetworkPeerId peer_id, DiscordNetworkChannelId channel_id, uint8_t* data, uint32_t data_length);
    void (*on_route_update)(void* event_data, const char* route_data);
};

struct IDiscordNetworkManager {
    void (*get_peer_id)(struct IDiscordNetworkManager* manager, DiscordNetworkPeerId* peer_id);
    enum EDiscordResult (*flush)(struct IDiscordNetworkManager* manager);
    enum EDiscordResult (*open_peer)(struct IDiscordNetworkManager* manager, DiscordNetworkPeerId peer_id, const char* route_data);
    enum EDiscordResult (*update_peer)(struct IDiscordNetworkManager* manager, DiscordNetworkPeerId peer_id, const char* route_data);
    enum EDiscordResult (*close_peer)(struct IDiscordNetworkManager* manager, DiscordNetworkPeerId peer_id);
    enum EDiscordResult (*open_channel)(struct IDiscordNetworkManager* manager, DiscordNetworkPeerId peer_id, DiscordNetworkChannelId channel_id, bool reliable);
    enum EDiscordResult (*close_channel)(struct IDiscordNetworkManager* manager, DiscordNetworkPeerId peer_id, DiscordNetworkChannelId channel_id);
    enum EDiscordResult (*send_message)(struct IDiscordNetworkManager* manager, DiscordNetworkPeerId peer_id, DiscordNetworkChannelId channel_id, uint8_t* data, uint32_t data_length);
};

struct IDiscordOverlayEvents {
    void (*on_toggle)(void* event_data, bool locked);
};

struct IDiscordOverlayManager {
    void (*is_enabled)(struct IDiscordOverlayManager* manager, bool* enabled);
    void (*is_locked)(struct IDiscordOverlayManager* manager, bool* locked);
    void (*set_locked)(struct IDiscordOverlayManager* manager, bool locked, void* callback_data, void (*callback)(void* callback_data, enum EDiscordResult result));
    void (*open_activity_invite)(struct IDiscordOverlayManager* manager, enum EDiscordActivityActionType type, void* callback_data, void (*callback)(void* callback_data, enum EDiscordResult result));
    void (*open_guild_invite)(struct IDiscordOverlayManager* manager, const char* code, void* callback_data, void (*callback)(void* callback_data, enum EDiscordResult result));
    void (*open_voice_settings)(struct IDiscordOverlayManager* manager, void* callback_data, void (*callback)(void* callback_data, enum EDiscordResult result));
};

typedef void* IDiscordStorageEvents;

struct IDiscordStorageManager {
    enum EDiscordResult (*read)(struct IDiscordStorageManager* manager, const char* name, uint8_t* data, uint32_t data_length, uint32_t* read);
    void (*read_async)(struct IDiscordStorageManager* manager, const char* name, void* callback_data, void (*callback)(void* callback_data, enum EDiscordResult result, uint8_t* data, uint32_t data_length));
    void (*read_async_partial)(struct IDiscordStorageManager* manager, const char* name, uint64_t offset, uint64_t length, void* callback_data, void (*callback)(void* callback_data, enum EDiscordResult result, uint8_t* data, uint32_t data_length));
    enum EDiscordResult (*write)(struct IDiscordStorageManager* manager, const char* name, uint8_t* data, uint32_t data_length);
    void (*write_async)(struct IDiscordStorageManager* manager, const char* name, uint8_t* data, uint32_t data_length, void* callback_data, void (*callback)(void* callback_data, enum EDiscordResult result));
    enum EDiscordResult (*delete_)(struct IDiscordStorageManager* manager, const char* name);
    enum EDiscordResult (*exists)(struct IDiscordStorageManager* manager, const char* name, bool* exists);
    void (*count)(struct IDiscordStorageManager* manager, int32_t* count);
    enum EDiscordResult (*stat)(struct IDiscordStorageManager* manager, const char* name, struct DiscordFileStat* stat);
    enum EDiscordResult (*stat_at)(struct IDiscordStorageManager* manager, int32_t index, struct DiscordFileStat* stat);
    enum EDiscordResult (*get_path)(struct IDiscordStorageManager* manager, DiscordPath* path);
};

struct IDiscordStoreEvents {
    void (*on_entitlement_create)(void* event_data, struct DiscordEntitlement* entitlement);
    void (*on_entitlement_delete)(void* event_data, struct DiscordEntitlement* entitlement);
};

struct IDiscordStoreManager {
    void (*fetch_skus)(struct IDiscordStoreManager* manager, void* callback_data, void (*callback)(void* callback_data, enum EDiscordResult result));
    void (*count_skus)(struct IDiscordStoreManager* manager, int32_t* count);
    enum EDiscordResult (*get_sku)(struct IDiscordStoreManager* manager, DiscordSnowflake sku_id, struct DiscordSku* sku);
    enum EDiscordResult (*get_sku_at)(struct IDiscordStoreManager* manager, int32_t index, struct DiscordSku* sku);
    void (*fetch_entitlements)(struct IDiscordStoreManager* manager, void* callback_data, void (*callback)(void* callback_data, enum EDiscordResult result));
    void (*count_entitlements)(struct IDiscordStoreManager* manager, int32_t* count);
    enum EDiscordResult (*get_entitlement)(struct IDiscordStoreManager* manager, DiscordSnowflake entitlement_id, struct DiscordEntitlement* entitlement);
    enum EDiscordResult (*get_entitlement_at)(struct IDiscordStoreManager* manager, int32_t index, struct DiscordEntitlement* entitlement);
    enum EDiscordResult (*has_sku_entitlement)(struct IDiscordStoreManager* manager, DiscordSnowflake sku_id, bool* has_entitlement);
    void (*start_purchase)(struct IDiscordStoreManager* manager, DiscordSnowflake sku_id, void* callback_data, void (*callback)(void* callback_data, enum EDiscordResult result));
};

struct IDiscordVoiceEvents {
    void (*on_settings_update)(void* event_data);
};

struct IDiscordVoiceManager {
    enum EDiscordResult (*get_input_mode)(struct IDiscordVoiceManager* manager, struct DiscordInputMode* input_mode);
    void (*set_input_mode)(struct IDiscordVoiceManager* manager, struct DiscordInputMode input_mode, void* callback_data, void (*callback)(void* callback_data, enum EDiscordResult result));
    enum EDiscordResult (*is_self_mute)(struct IDiscordVoiceManager* manager, bool* mute);
    enum EDiscordResult (*set_self_mute)(struct IDiscordVoiceManager* manager, bool mute);
    enum EDiscordResult (*is_self_deaf)(struct IDiscordVoiceManager* manager, bool* deaf);
    enum EDiscordResult (*set_self_deaf)(struct IDiscordVoiceManager* manager, bool deaf);
    enum EDiscordResult (*is_local_mute)(struct IDiscordVoiceManager* manager, DiscordSnowflake user_id, bool* mute);
    enum EDiscordResult (*set_local_mute)(struct IDiscordVoiceManager* manager, DiscordSnowflake user_id, bool mute);
    enum EDiscordResult (*get_local_volume)(struct IDiscordVoiceManager* manager, DiscordSnowflake user_id, uint8_t* volume);
    enum EDiscordResult (*set_local_volume)(struct IDiscordVoiceManager* manager, DiscordSnowflake user_id, uint8_t volume);
};

struct IDiscordAchievementEvents {
    void (*on_user_achievement_update)(void* event_data, struct DiscordUserAchievement* user_achievement);
};

struct IDiscordAchievementManager {
    void (*set_user_achievement)(struct IDiscordAchievementManager* manager, DiscordSnowflake achievement_id, uint8_t percent_complete, void* callback_data, void (*callback)(void* callback_data, enum EDiscordResult result));
    void (*fetch_user_achievements)(struct IDiscordAchievementManager* manager, void* callback_data, void (*callback)(void* callback_data, enum EDiscordResult result));
    void (*count_user_achievements)(struct IDiscordAchievementManager* manager, int32_t* count);
    enum EDiscordResult (*get_user_achievement)(struct IDiscordAchievementManager* manager, DiscordSnowflake user_achievement_id, struct DiscordUserAchievement* user_achievement);
    enum EDiscordResult (*get_user_achievement_at)(struct IDiscordAchievementManager* manager, int32_t index, struct DiscordUserAchievement* user_achievement);
};

typedef void* IDiscordCoreEvents;

struct IDiscordCore {
    void (*destroy)(struct IDiscordCore* core);
    enum EDiscordResult (*run_callbacks)(struct IDiscordCore* core);
    void (*set_log_hook)(struct IDiscordCore* core, enum EDiscordLogLevel min_level, void* hook_data, void (*hook)(void* hook_data, enum EDiscordLogLevel level, const char* message));
    struct IDiscordApplicationManager* (*get_application_manager)(struct IDiscordCore* core);
    struct IDiscordUserManager* (*get_user_manager)(struct IDiscordCore* core);
    struct IDiscordImageManager* (*get_image_manager)(struct IDiscordCore* core);
    struct IDiscordActivityManager* (*get_activity_manager)(struct IDiscordCore* core);
    struct IDiscordRelationshipManager* (*get_relationship_manager)(struct IDiscordCore* core);
    struct IDiscordLobbyManager* (*get_lobby_manager)(struct IDiscordCore* core);
    struct IDiscordNetworkManager* (*get_network_manager)(struct IDiscordCore* core);
    struct IDiscordOverlayManager* (*get_overlay_manager)(struct IDiscordCore* core);
    struct IDiscordStorageManager* (*get_storage_manager)(struct IDiscordCore* core);
    struct IDiscordStoreManager* (*get_store_manager)(struct IDiscordCore* core);
    struct IDiscordVoiceManager* (*get_voice_manager)(struct IDiscordCore* core);
    struct IDiscordAchievementManager* (*get_achievement_manager)(struct IDiscordCore* core);
};