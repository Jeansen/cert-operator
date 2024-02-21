package certmanger

import io.fabric8.kubernetes.api.model.certificates.v1.CertificateSigningRequest
import io.fabric8.kubernetes.client.KubernetesClient
import io.fabric8.kubernetes.client.KubernetesClientException
import io.javaoperatorsdk.operator.api.reconciler.Context
import io.javaoperatorsdk.operator.api.reconciler.ControllerConfiguration
import io.javaoperatorsdk.operator.api.reconciler.Reconciler
import io.javaoperatorsdk.operator.api.reconciler.UpdateControl
import io.quarkiverse.operatorsdk.annotations.RBACRule
import io.quarkiverse.operatorsdk.annotations.RBACVerbs
import java.util.logging.Level
import java.util.logging.Logger


@ControllerConfiguration(name = "server-cert-approve")
@RBACRule(
    verbs = [RBACVerbs.UPDATE, RBACVerbs.LIST, RBACVerbs.WATCH],
    apiGroups = ["certificates.k8s.io"],
    resources = ["certificatesigningrequests"]
)
@RBACRule(
    verbs = [RBACVerbs.UPDATE, RBACVerbs.GET, RBACVerbs.CREATE],
    apiGroups = ["coordination.k8s.io"],
    resources = ["leases"],
)
@RBACRule(
    verbs = [RBACVerbs.UPDATE],
    apiGroups = ["certificates.k8s.io"],
    resources = ["certificatesigningrequests/approval"]
)
@RBACRule(
    verbs = ["approve"],
    apiGroups = ["certificates.k8s.io"],
    resources = ["signers"],
    resourceNames = ["kubernetes.io/kubelet-serving"]
)
@RBACRule(
    verbs = ["create"],
    apiGroups = [""],
    resources = ["events"]
)


class ExposedAppReconciler(private val client: KubernetesClient) : Reconciler<CertificateSigningRequest?> {
    override fun reconcile(resource: CertificateSigningRequest?, context: Context<CertificateSigningRequest?>?): UpdateControl<CertificateSigningRequest?> {
        val logger = Logger.getLogger(ExposedAppReconciler::class.toString())

        logger.log(Level.INFO, "Incoming CSR: ${resource?.metadata?.name}")

        try {
            val r = client.certificates().v1().certificateSigningRequests().resource(resource)
            r.item().status.conditions.find { it.type == "Approved" } ?: also {
                logger.log(Level.INFO, "Found pending CSR: ${resource?.metadata?.name}")
                r.approve()
                logger.log(Level.INFO, "Approved CSR ${resource?.metadata?.name}")
                return UpdateControl.patchStatus(resource)
            }
        } catch (e: KubernetesClientException) {
            logger.log(Level.WARNING, e.stackTraceToString())
            logger.log(Level.WARNING, "Server Timeout")
        }

        logger.log(Level.INFO, "CSR: ${resource?.metadata?.name} already approved")
        return UpdateControl.noUpdate()
    }
}
