package certmanger

import io.fabric8.kubernetes.api.model.certificates.v1.CertificateSigningRequest
import io.fabric8.kubernetes.client.KubernetesClient
import io.fabric8.kubernetes.client.KubernetesClientException
import io.javaoperatorsdk.operator.api.reconciler.Context
import io.javaoperatorsdk.operator.api.reconciler.ControllerConfiguration
import io.javaoperatorsdk.operator.api.reconciler.Reconciler
import io.javaoperatorsdk.operator.api.reconciler.UpdateControl
import java.util.logging.Level
import java.util.logging.Logger

@ControllerConfiguration(name = "server-cert-approve")
class ExposedAppReconciler(private val client: KubernetesClient) : Reconciler<CertificateSigningRequest?> {

    override fun reconcile(resource: CertificateSigningRequest?, context: Context<CertificateSigningRequest?>?): UpdateControl<CertificateSigningRequest?> {
        val logger = Logger.getLogger(ExposedAppReconciler::class.toString())

        logger.log(Level.INFO, "Found new pending CSR: ${resource?.metadata?.name}")
        try {
            val r = client.certificates().v1().certificateSigningRequests().resource(resource)
            r.item().status.conditions.find { it.type != "Approved" } ?: {
                logger.log(Level.INFO, "Approving ${resource?.metadata?.name}")
                r.approve()
            }
        } catch (e: KubernetesClientException) {
            logger.log(Level.WARNING, "Server Timeout")
        }

        return UpdateControl.patchStatus(resource)
    }
}
