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

const val CERTIFICATES_K8S_IO_GROUP = "certificates.k8s.io"
const val ADDITIONAL_UPDATE_RESOURCE = "certificatesigningrequests/approval"
const val SIGNERS_VERB = "approve"
const val SIGNERS_RESOURCE = "signers"
const val SIGNERS_RESOURCE_NAMES = "kubernetes.io/kubelet-serving"

@ControllerConfiguration(name = "server-cert-approve")
@RBACRule(verbs = [RBACVerbs.UPDATE], apiGroups = [CERTIFICATES_K8S_IO_GROUP], resources = [ADDITIONAL_UPDATE_RESOURCE])
@RBACRule(verbs = [SIGNERS_VERB], apiGroups = [CERTIFICATES_K8S_IO_GROUP], resources = [SIGNERS_RESOURCE], resourceNames = [SIGNERS_RESOURCE_NAMES])
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
