package ru.mail.teamcity.ssh.web;

import jetbrains.buildServer.serverSide.BuildAgentManager;
import jetbrains.buildServer.serverSide.SBuildAgent;
import jetbrains.buildServer.web.openapi.PagePlaces;
import jetbrains.buildServer.web.openapi.PlaceId;
import jetbrains.buildServer.web.openapi.PluginDescriptor;
import jetbrains.buildServer.web.openapi.SimplePageExtension;
import jetbrains.buildServer.web.openapi.agent.AgentFinderUtil;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import javax.servlet.http.HttpServletRequest;
import java.util.Map;

/**
 * User: g.chernyshev
 * Date: 30/05/15
 * Time: 10:17
 */
public class AgentWebSshExtension extends SimplePageExtension {

    @NotNull
    private final BuildAgentManager agentManager;

    public AgentWebSshExtension(@NotNull PagePlaces pagePlaces, @NotNull PluginDescriptor pluginDescriptor, @NotNull BuildAgentManager agentManager) {
        super(
                pagePlaces,
                PlaceId.AGENT_SUMMARY,
                "webSsh",
                pluginDescriptor.getPluginResourcesPath("webSshAgent.jsp")
        );
        this.agentManager = agentManager;
        register();
    }

    @Override
    public boolean isAvailable(@NotNull HttpServletRequest request) {
        SBuildAgent agent = getAgent(request);
        return (agent != null);
    }

    @Override
    public void fillModel(@NotNull Map<String, Object> model, @NotNull HttpServletRequest request) {
        SBuildAgent agent = getAgent(request);
        if (null != agent) {
            String ip = agent.getHostAddress();
            String publicIp = agent.getBuildParameters().get("system.ec2.public-hostname");
            if (null != publicIp) {
                ip = publicIp;
            }
            model.put("agentIp", ip);
        }
        super.fillModel(model, request);
    }


    @Nullable
    private SBuildAgent getAgent(HttpServletRequest request) {
        return AgentFinderUtil.findAgent(request, this.agentManager);
    }
}
