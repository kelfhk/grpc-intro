package com.kelvin.client.loadbalancing;

import io.grpc.EquivalentAddressGroup;
import io.grpc.NameResolver;

import java.util.List;

public class TempNameResolver extends NameResolver {
    private String service;

    public TempNameResolver(String service) {
        this.service = service;
    }

    @Override
    public void start(Listener2 listener) {
        List<EquivalentAddressGroup> addressGroups = ServiceRegistry.getInstances(this.service);
        ResolutionResult resolutionResult = ResolutionResult.newBuilder().setAddresses(addressGroups).build();
        listener.onResult(resolutionResult);
    }

    @Override
    public void refresh() {
        super.refresh();
    }

    @Override
    public String getServiceAuthority() {
        return "temp";
    }

    @Override
    public void shutdown() {

    }

}
