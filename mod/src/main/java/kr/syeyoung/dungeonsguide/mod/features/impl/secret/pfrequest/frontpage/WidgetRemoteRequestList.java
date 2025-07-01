package kr.syeyoung.dungeonsguide.mod.features.impl.secret.pfrequest.frontpage;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import kr.syeyoung.dungeonsguide.launcher.auth.AuthManager;
import kr.syeyoung.dungeonsguide.mod.features.FeatureRegistry;
import kr.syeyoung.dungeonsguide.mod.features.impl.party.playerpreview.api.ApiFetcher;
import kr.syeyoung.dungeonsguide.mod.features.impl.secret.pfrequest.FeatureRequestCalculation;
import kr.syeyoung.dungeonsguide.mod.features.impl.secret.pfrequest.remotereq.RemoteCache;
import kr.syeyoung.dungeonsguide.mod.features.impl.secret.pfrequest.remotereq.WidgetRequestDetails;
import kr.syeyoung.dungeonsguide.mod.gui.BindableAttribute;
import kr.syeyoung.dungeonsguide.mod.gui.elements.Column;
import kr.syeyoung.dungeonsguide.mod.gui.elements.Navigator;
import kr.syeyoung.dungeonsguide.mod.gui.xml.AnnotatedImportOnlyWidget;
import kr.syeyoung.dungeonsguide.mod.gui.xml.annotations.Bind;
import kr.syeyoung.dungeonsguide.mod.gui.xml.annotations.On;
import kr.syeyoung.modapi.ModAPI;
import kr.syeyoung.modapi.data.ResourceIdentifier;

import java.io.IOException;

public class WidgetRemoteRequestList extends AnnotatedImportOnlyWidget {
    @Bind(variableName = "requestsApi")
    public final BindableAttribute<Column> requestsApi = new BindableAttribute<>(Column.class);
    @Bind(variableName = "err")
    public final BindableAttribute<String> err = new BindableAttribute<>(String.class);

    public WidgetRemoteRequestList() {
        super(new ResourceIdentifier("dungeonsguide:gui/features/requestcalculation/frontpage/remoterequestlist.gui"));
    }

    private void doReload() {
        try {
            JsonObject jsonObject = ApiFetcher.getJsonWithAuth(FeatureRequestCalculation.DOMAIN+"/info", AuthManager.getInstance().getWorkingTokenOrThrow());
            requestsApi.getValue().removeAllWidget();
            for (JsonElement requestId : jsonObject.getAsJsonArray("requests")) {
                String reqId = requestId.getAsString();

                RemoteCache cache = FeatureRegistry.SECRET_PATHFIND_REQUEST.getRemoteCacheMap().get(reqId);
                WidgetRemoteRequest reqSet = new WidgetRemoteRequest(this, requestId.getAsString(), cache);
                requestsApi.getValue().addWidget(reqSet);
            }
        } catch (IOException e) {
            this.err.setValue(e.getMessage());
            e.printStackTrace();
        }

    }

    @On(functionName = "reload")
    public void reload() {
        ModAPI.getAPI().getSoundHandler().playSoundAtPlayer(new ResourceIdentifier("gui.button.press"), 1.0F);
        requestsApi.getValue().removeAllWidget();
        ApiFetcher.ex.submit(this::doReload);
    }


    @Override
    public void onMount() {
        reload();
    }

    public class WidgetRemoteRequest extends AnnotatedImportOnlyWidget {
        @Bind(variableName = "backgroundColor")
        public final BindableAttribute<Integer> backgroundColor = new BindableAttribute<>(Integer.class);

        @Bind(variableName = "name")
        public final BindableAttribute<String> name = new BindableAttribute<>(String.class);

        private WidgetRemoteRequestList parent;
        private String requestId;
        private RemoteCache cache;

        public WidgetRemoteRequest(WidgetRemoteRequestList parent, String requestId, RemoteCache cache) {
            super(new ResourceIdentifier("dungeonsguide:gui/features/requestcalculation/frontpage/remoterequest.gui"));

            this.parent = parent;
            this.requestId = requestId;
            this.cache = cache;

            if (cache == null) {
                backgroundColor.setValue( 0xffaa0000);

                name.setValue("Unknown request:: reqid="+requestId);
            } else {
                backgroundColor.setValue(cache.isCheckedAfterComplete() ? 0xFF505050 :
                        cache.isWasInProgress() ? 0xFF575600 : 0xff065702);
                name.setValue(cache.getName());
            }
        }

        @Override
        public void onMount() {
            if (cache != null) {
                backgroundColor.setValue(cache.isCheckedAfterComplete() ? 0xFF505050 :
                        cache.isWasInProgress() ? 0xFF575600 : 0xff065702);
            }
        }


        @On(functionName = "view")
        public void view() {
            ModAPI.getAPI().getSoundHandler().playSoundAtPlayer(new ResourceIdentifier("gui.button.press"), 1.0F);

            Navigator.getNavigator(getDomElement()).openPage(new WidgetRequestDetails(requestId));
        }
    }


}
