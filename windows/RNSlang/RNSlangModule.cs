using ReactNative.Bridge;
using System;
using System.Collections.Generic;
using Windows.ApplicationModel.Core;
using Windows.UI.Core;

namespace Slang.RNSlang
{
    /// <summary>
    /// A module that allows JS to share data.
    /// </summary>
    class RNSlangModule : NativeModuleBase
    {
        /// <summary>
        /// Instantiates the <see cref="RNSlangModule"/>.
        /// </summary>
        internal RNSlangModule()
        {

        }

        /// <summary>
        /// The name of the native module.
        /// </summary>
        public override string Name
        {
            get
            {
                return "RNSlang";
            }
        }
    }
}
