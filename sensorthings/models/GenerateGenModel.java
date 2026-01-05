import org.eclipse.emf.codegen.ecore.genmodel.GenModel;
import org.eclipse.emf.codegen.ecore.genmodel.util.GenModelUtil;
import org.eclipse.emf.ecore.resource.Resource;
import org.eclipse.emf.ecore.resource.impl.ResourceSetImpl;
import org.eclipse.emf.ecore.xmi.impl.XMIResourceFactoryImpl;
import org.eclipse.emf.common.util.URI;

import java.io.File;

public class GenerateGenModel {
    public static void main(String[] args) throws Exception {
        if (args.length != 2) {
            System.out.println("Usage: java GenerateGenModel <input.ecore> <output.genmodel>");
            return;
        }

        String ecorePath = args[0];
        String genmodelPath = args[1];

        ResourceSetImpl resourceSet = new ResourceSetImpl();
        resourceSet.getResourceFactoryRegistry().getExtensionToFactoryMap().put("ecore", new XMIResourceFactoryImpl());
        resourceSet.getResourceFactoryRegistry().getExtensionToFactoryMap().put("genmodel", new XMIResourceFactoryImpl());

        Resource ecoreResource = resourceSet.getResource(URI.createFileURI(new File(ecorePath).getAbsolutePath()), true);
        GenModel genModel = GenModelUtil.createGenModel(ecoreResource.getContents().get(0));

        Resource genResource = resourceSet.createResource(URI.createFileURI(new File(genmodelPath).getAbsolutePath()));
        genResource.getContents().add(genModel);
        genResource.save(null);

        System.out.println("GenModel generated successfully: " + genmodelPath);
    }
}

