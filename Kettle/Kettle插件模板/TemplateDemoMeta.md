```java
package com.epoint.bigdata.datacleansing.trans.template;

import com.epoint.bigdata.datacleansing.trans.common.EmptyStepData;
import org.pentaho.di.core.annotations.Step;
import org.pentaho.di.core.database.DatabaseMeta;
import org.pentaho.di.core.exception.KettleException;
import org.pentaho.di.core.exception.KettleXMLException;
import org.pentaho.di.repository.ObjectId;
import org.pentaho.di.repository.Repository;
import org.pentaho.di.trans.Trans;
import org.pentaho.di.trans.TransMeta;
import org.pentaho.di.trans.step.*;
import org.pentaho.metastore.api.IMetaStore;
import org.w3c.dom.Node;

import java.util.List;

/**
 * 2019年09月09日 superz add
 */
@Step(id = "TemplateDemo", name = "插件模板")
public class TemplateDemoMeta extends BaseStepMeta implements StepMetaInterface
{
    @Override
    public void setDefault() {

    }

    @Override
    public StepInterface getStep(StepMeta stepMeta, StepDataInterface stepDataInterface, int copyNr,
            TransMeta transMeta, Trans trans) {
        return new TemplateDemo(stepMeta, stepDataInterface, copyNr, transMeta, trans);
    }

    @Override
    public StepDataInterface getStepData() {
        return new EmptyStepData();
    }

    // 序列化和反序列化=====================================================================

    @Override
    public void loadXML(Node stepnode, List<DatabaseMeta> databases, IMetaStore metaStore) throws KettleXMLException {

    }

    @Override
    public String getXML() throws KettleException {
        return super.getXML();
    }

    @Override
    public void readRep(Repository rep, IMetaStore metaStore, ObjectId id_step, List<DatabaseMeta> databases)
            throws KettleException {

    }

    @Override
    public void saveRep(Repository rep, IMetaStore metaStore, ObjectId id_transformation, ObjectId id_step)
            throws KettleException {
    }

    // 序列化和反序列化=====================================================================

    // getter/setter=======================================================================
    // getter/setter========================================================================
}
```

