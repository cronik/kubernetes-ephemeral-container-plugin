package io.jenkins.plugins.kubernetes.ephemeral;

import edu.umd.cs.findbugs.annotations.NonNull;
import hudson.Launcher;
import hudson.model.Node;
import java.io.Serial;
import org.csanchez.jenkins.plugins.kubernetes.KubernetesSlave;
import org.csanchez.jenkins.plugins.kubernetes.pipeline.ContainerExecDecorator;

/**
 * {@link EphemeralContainerExecDecorator} is a {@link hudson.LauncherDecorator} specifically for containers launched
 * via an {@link EphemeralContainerStep}. This is exactly the same as {@link ContainerExecDecorator} but with
 * a different class name. The main purpose for this class is to get around the unfortunate bit of code in the
 * pipeline-maven-plugin which explicitly detects {@link ContainerExecDecorator} as "not a container". Not
 * sure the history behind it, but because it's not treated as a container env the maven bin is not correctly
 * determined because it uses the main Pod container env rather than introspecting the current Pod container
 * context.
 */
public class EphemeralContainerExecDecorator extends ContainerExecDecorator {

    @Serial
    private static final long serialVersionUID = -168729465605848851L;

    @Override
    @NonNull
    public Launcher decorate(@NonNull final Launcher launcher, @NonNull final Node node) {
        // Allows other nodes to be provisioned inside the container clause
        // If the node is not a KubernetesSlave return the original launcher
        if (!(node instanceof KubernetesSlave)) {
            return launcher;
        }

        return new EphemeralContainerDecoratedLauncher(super.decorate(launcher, node));
    }

    /**
     * DecoratedLauncher that ensure withMaven executor think we are within a container.
     * <a href="https://github.com/jenkinsci/pipeline-maven-plugin/blob/f299f892b632d5a9fe7e3ccdc06a41c86e0b3d7e/jenkins-plugin/src/main/java/org/jenkinsci/plugins/pipeline/maven/WithMavenStepExecution2.java#L252-L254">...</a>
     */
    static class EphemeralContainerDecoratedLauncher extends Launcher.DecoratedLauncher {

        public EphemeralContainerDecoratedLauncher(@NonNull Launcher inner) {
            super(inner);
        }
    }
}
